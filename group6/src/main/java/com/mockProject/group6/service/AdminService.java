package com.mockProject.group6.service;

import com.mockProject.group6.entity.Role;
import com.mockProject.group6.entity.User;
import com.mockProject.group6.repository.RoleRepository;
import com.mockProject.group6.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AdminService {

    private static final Set<String> COURSE_STATUSES =
            Set.of("DRAFT", "PENDING", "PUBLISHED", "REJECTED");
    private static final Set<String> LOG_TYPES =
            Set.of("ACCOUNT", "PAYMENT", "ENROLLMENT", "COMPLETION");

    private final JdbcTemplate jdbc;
    private final UserRepository users;
    private final RoleRepository roles;

    public AdminService(JdbcTemplate jdbc, UserRepository users, RoleRepository roles) {
        this.jdbc = jdbc;
        this.users = users;
        this.roles = roles;
    }

    public List<Map<String, Object>> categories() {
        return jdbc.queryForList("""
                SELECT CategoryId AS categoryId, Name AS name
                FROM dbo.Categories
                ORDER BY Name
                """);
    }

    public Map<String, Object> addCategory(Map<String, Object> body) {
        String name = requiredText(body, "name", 50);
        if (exists("SELECT COUNT(*) FROM dbo.Categories WHERE LOWER(Name) = LOWER(?)", name)) {
            throw new AdminApiException(409, "Category already exists.");
        }

        int id = insertId("INSERT INTO dbo.Categories (Name) VALUES (?)", name);
        return category(id, name);
    }

    public Map<String, Object> renameCategory(int id, Map<String, Object> body) {
        requireExists("SELECT COUNT(*) FROM dbo.Categories WHERE CategoryId = ?", id, "Category not found.");
        String name = requiredText(body, "name", 50);
        if (exists("""
                SELECT COUNT(*) FROM dbo.Categories
                WHERE LOWER(Name) = LOWER(?) AND CategoryId <> ?
                """, name, id)) {
            throw new AdminApiException(409, "Category already exists.");
        }
        jdbc.update("UPDATE dbo.Categories SET Name = ? WHERE CategoryId = ?", name, id);
        return category(id, name);
    }

    public void deleteCategory(int id) {
        requireExists("SELECT COUNT(*) FROM dbo.Categories WHERE CategoryId = ?", id, "Category not found.");
        if (exists("SELECT COUNT(*) FROM dbo.Courses WHERE CategoryId = ?", id)) {
            throw new AdminApiException(409, "Cannot delete a category that has courses.");
        }
        jdbc.update("DELETE FROM dbo.Categories WHERE CategoryId = ?", id);
    }

    public List<Map<String, Object>> courses() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT c.CourseId AS courseId, c.Title AS title, c.Description AS description,
                       c.CategoryId AS categoryId, cat.Name AS category,
                       c.CourseLevel AS level, c.Price AS price, c.ImageUrl AS imageUrl,
                       c.Status AS status, c.RejectReason AS rejectReason,
                       c.InstructorId AS instructorId, u.fullname AS instructorName,
                       COUNT(DISTINCT e.EnrollmentId) AS enrolledCount,
                       COUNT(DISTINCT l.LessonId) AS lessonCount
                FROM dbo.Courses c
                JOIN dbo.Categories cat ON cat.CategoryId = c.CategoryId
                JOIN dbo.users u ON u.id = c.InstructorId
                LEFT JOIN dbo.Enrollments e ON e.CourseId = c.CourseId
                LEFT JOIN dbo.Sections s ON s.CourseId = c.CourseId
                LEFT JOIN dbo.Lessons l ON l.SectionId = s.SectionId
                GROUP BY c.CourseId, c.Title, c.Description, c.CategoryId, cat.Name,
                         c.CourseLevel, c.Price, c.ImageUrl, c.Status, c.RejectReason,
                         c.InstructorId, u.fullname, c.CreatedAt
                ORDER BY c.CreatedAt DESC, c.CourseId DESC
                """);

        for (Map<String, Object> row : rows) {
            row.put("instructor", instructor(row));
        }
        return rows;
    }

    public Map<String, Object> createCourse(Map<String, Object> body, User admin) {
        CourseInput input = courseInput(body, admin.getId(), false);
        int id = insertId("""
                INSERT INTO dbo.Courses
                    (Title, Description, CategoryId, CourseLevel, Price, ImageUrl,
                     Status, RejectReason, InstructorId)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, input.title, input.description, input.categoryId, input.level,
                input.price, input.imageUrl, input.status, input.rejectReason, input.instructorId);
        return course(id);
    }

    public Map<String, Object> updateCourse(int id, Map<String, Object> body, User admin) {
        course(id);
        CourseInput input = courseInput(body, admin.getId(), true);
        jdbc.update("""
                UPDATE dbo.Courses
                SET Title = ?, Description = ?, CategoryId = ?, CourseLevel = ?,
                    Price = ?, ImageUrl = ?, Status = ?, RejectReason = ?, InstructorId = ?
                WHERE CourseId = ?
                """, input.title, input.description, input.categoryId, input.level,
                input.price, input.imageUrl, input.status, input.rejectReason, input.instructorId, id);
        return course(id);
    }

    @Transactional
    public void deleteCourse(int id) {
        course(id);
        jdbc.update("DELETE FROM dbo.Courses WHERE CourseId = ?", id);
    }

    public Map<String, Object> approveCourse(int id) {
        Map<String, Object> course = course(id);
        if (!"PENDING".equals(course.get("status"))) {
            throw new AdminApiException(409, "Only pending courses can be approved.");
        }
        jdbc.update("UPDATE dbo.Courses SET Status = 'PUBLISHED', RejectReason = NULL WHERE CourseId = ?", id);
        log("ACCOUNT", "COURSE_APPROVED", null, null, id, String.valueOf(course.get("title")), null, null, null, null);
        return course(id);
    }

    public Map<String, Object> rejectCourse(int id, Map<String, Object> body) {
        Map<String, Object> course = course(id);
        String reason = optionalText(body, "rejectReason", 500);
        jdbc.update("""
                UPDATE dbo.Courses SET Status = 'REJECTED', RejectReason = ?
                WHERE CourseId = ?
                """, reason.isBlank() ? null : reason, id);
        log("ACCOUNT", "COURSE_REJECTED", null, null, id, String.valueOf(course.get("title")), null, null, reason, null);
        return course(id);
    }

    public Map<String, Object> course(int id) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT c.CourseId AS courseId, c.Title AS title, c.Description AS description,
                       c.CategoryId AS categoryId, cat.Name AS category,
                       c.CourseLevel AS level, c.Price AS price, c.ImageUrl AS imageUrl,
                       c.Status AS status, c.RejectReason AS rejectReason,
                       c.InstructorId AS instructorId, u.fullname AS instructorName,
                       (SELECT COUNT(*) FROM dbo.Enrollments e WHERE e.CourseId = c.CourseId) AS enrolledCount,
                       (SELECT COUNT(*) FROM dbo.Lessons l
                          JOIN dbo.Sections s ON s.SectionId = l.SectionId
                          WHERE s.CourseId = c.CourseId) AS lessonCount
                FROM dbo.Courses c
                JOIN dbo.Categories cat ON cat.CategoryId = c.CategoryId
                JOIN dbo.users u ON u.id = c.InstructorId
                WHERE c.CourseId = ?
                """, id);
        if (rows.isEmpty()) {
            throw new AdminApiException(404, "Course not found.");
        }
        Map<String, Object> result = rows.get(0);
        result.put("instructor", instructor(result));
        result.put("sections", sections(id));
        return result;
    }

    public List<Map<String, Object>> users() {
        List<User> all = users.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : all) {
            String role = user.getRole() == null ? "STUDENT" : user.getRole().getName();
            int id = user.getId();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("userId", id);
            row.put("fullName", user.getFullName());
            row.put("email", user.getEmail());
            row.put("role", "ADMIN".equalsIgnoreCase(role) ? "ADMIN" : "STUDENT");
            row.put("author", count("SELECT COUNT(*) FROM dbo.Courses WHERE InstructorId = ?", id) > 0);
            row.put("locked", Boolean.TRUE.equals(user.getLocked()));
            row.put("verified", Boolean.TRUE.equals(user.getEmailVerified()));
            row.put("createdAt", user.getUpdatedAt());
            row.put("enrollments", count("SELECT COUNT(*) FROM dbo.Enrollments WHERE UserId = ?", id));
            row.put("courses", count("SELECT COUNT(*) FROM dbo.Courses WHERE InstructorId = ?", id));
            result.add(row);
        }
        return result;
    }

    @Transactional
    public void changeRole(User admin, int id, Map<String, Object> body) {
        User target = findUser(id);
        if (id == admin.getId()) {
            throw new AdminApiException(400, "You cannot change your own role.");
        }

        String requested = optionalText(body, "role", 30).toUpperCase(Locale.ROOT);
        if (!Set.of("ADMIN", "STUDENT").contains(requested)) {
            throw new AdminApiException(400, "Invalid role.");
        }
        String roleName = requested;
        Role role = roles.findByName(roleName)
                .or(() -> roles.findByName("STUDENT"))
                .or(() -> roles.findByName("MEMBER"))
                .or(() -> roles.findByName("USER"))
                .orElseThrow(() -> new AdminApiException(400, "A student role is not configured."));
        String previous = target.getRole() == null ? "" : target.getRole().getName();
        target.setRole(role);
        users.save(target);
        log("ACCOUNT", "ROLE_CHANGED", id, target.getEmail(), null, null, null, null,
                previous + " -> " + role.getName(), admin.getFullName());
    }

    @Transactional
    public void lockUser(User admin, int id, Map<String, Object> body) {
        User target = findUser(id);
        if (id == admin.getId()) {
            throw new AdminApiException(400, "You cannot lock your own account.");
        }
        Object value = body.get("locked");
        if (!(value instanceof Boolean locked)) {
            throw new AdminApiException(400, "A locked value is required.");
        }
        target.setLocked(locked);
        users.save(target);
        log("ACCOUNT", locked ? "LOCKED" : "UNLOCKED", id, target.getEmail(),
                null, null, null, null, null, admin.getFullName());
    }

    public List<Map<String, Object>> tickets() {
        return jdbc.queryForList("""
                SELECT t.TicketId AS ticketId, t.UserId AS userId, t.Name AS name,
                       t.Email AS email, t.Category AS category, t.CourseId AS courseId,
                       c.Title AS courseTitle, t.Subject AS subject, t.Message AS message,
                       t.Status AS status, t.Reply AS reply, t.RepliedAt AS repliedAt,
                       t.CreatedAt AS createdAt
                FROM dbo.SupportTickets t
                LEFT JOIN dbo.Courses c ON c.CourseId = t.CourseId
                ORDER BY t.CreatedAt DESC, t.TicketId DESC
                """);
    }

    public Map<String, Object> updateTicket(int id, Map<String, Object> body) {
        Map<String, Object> ticket = one("""
                SELECT TicketId AS ticketId, Status AS status
                FROM dbo.SupportTickets WHERE TicketId = ?
                """, id, "Ticket not found.");
        String status = optionalText(body, "status", 10);
        if (status.isBlank()) {
            status = String.valueOf(ticket.get("status"));
        }
        status = status.toUpperCase(Locale.ROOT);
        if (!Set.of("OPEN", "RESOLVED").contains(status)) {
            throw new AdminApiException(400, "Invalid ticket status.");
        }
        String reply = optionalText(body, "reply", 1000);
        if (reply.isBlank()) {
            jdbc.update("UPDATE dbo.SupportTickets SET Status = ? WHERE TicketId = ?", status, id);
        } else {
            jdbc.update("""
                    UPDATE dbo.SupportTickets
                    SET Status = ?, Reply = ?, RepliedAt = SYSDATETIME()
                    WHERE TicketId = ?
                    """, status, reply, id);
        }
        return one("""
                SELECT t.TicketId AS ticketId, t.UserId AS userId, t.Name AS name,
                       t.Email AS email, t.Category AS category, t.CourseId AS courseId,
                       c.Title AS courseTitle, t.Subject AS subject, t.Message AS message,
                       t.Status AS status, t.Reply AS reply, t.RepliedAt AS repliedAt,
                       t.CreatedAt AS createdAt
                FROM dbo.SupportTickets t LEFT JOIN dbo.Courses c ON c.CourseId = t.CourseId
                WHERE t.TicketId = ?
                """, id, "Ticket not found.");
    }

    public List<Map<String, Object>> logs(String requestedType) {
        String type = requestedType == null ? "" : requestedType.toUpperCase(Locale.ROOT);
        if (!LOG_TYPES.contains(type)) {
            throw new AdminApiException(404, "Log type not found.");
        }
        return jdbc.queryForList("""
                SELECT TOP 500 l.LogId AS logId, l.LogType AS type, l.Action AS action,
                       l.UserId AS userId, u.fullname AS userName, l.Email AS email,
                       l.CourseId AS courseId, l.CourseTitle AS courseTitle, l.Amount AS amount,
                       l.PaymentMethod AS method, l.Detail AS detail, l.ActorName AS actor,
                       l.CreatedAt AS createdAt
                FROM dbo.ActivityLogs l
                LEFT JOIN dbo.users u ON u.id = l.UserId
                WHERE l.LogType = ?
                ORDER BY l.CreatedAt DESC, l.LogId DESC
                """, type);
    }

    public List<Map<String, Object>> reviews() {
        return jdbc.queryForList("""
                SELECT TOP 50 r.ReviewId AS reviewId, c.Title AS courseTitle,
                       u.fullname AS userName, r.Rating AS rating,
                       r.Comment AS comment, r.CreatedAt AS createdAt
                FROM dbo.Reviews r
                JOIN dbo.Courses c ON c.CourseId = r.CourseId
                JOIN dbo.users u ON u.id = r.UserId
                ORDER BY r.ReviewId DESC
                """);
    }

    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("users", count("SELECT COUNT(*) FROM dbo.users"));
        result.put("courses", count("SELECT COUNT(*) FROM dbo.Courses"));
        result.put("published", count("SELECT COUNT(*) FROM dbo.Courses WHERE Status = 'PUBLISHED'"));
        result.put("pending", count("SELECT COUNT(*) FROM dbo.Courses WHERE Status = 'PENDING'"));
        result.put("enrollments", count("SELECT COUNT(*) FROM dbo.Enrollments"));
        result.put("reviews", count("SELECT COUNT(*) FROM dbo.Reviews"));
        result.put("openTickets", count("SELECT COUNT(*) FROM dbo.SupportTickets WHERE Status = 'OPEN'"));
        result.put("topCourses", jdbc.queryForList("""
                SELECT TOP 5 c.CourseId AS courseId, c.Title AS title,
                       COUNT(e.EnrollmentId) AS enrolled
                FROM dbo.Courses c
                LEFT JOIN dbo.Enrollments e ON e.CourseId = c.CourseId
                WHERE c.Status = 'PUBLISHED'
                GROUP BY c.CourseId, c.Title
                ORDER BY COUNT(e.EnrollmentId) DESC, c.CourseId
                """));
        return result;
    }

    public Map<String, Object> addSupportTicket(Map<String, Object> body, User user) {
        String name = requiredText(body, "name", 100);
        String email = requiredText(body, "email", 150);
        String category = optionalText(body, "category", 10).toUpperCase(Locale.ROOT);
        if (category.isBlank()) {
            category = "OTHER";
        } else if (!Set.of("PAYMENT", "COURSE", "ACCOUNT", "OTHER").contains(category)) {
            throw new AdminApiException(400, "Invalid support category.");
        }
        String subject = requiredText(body, "subject", 100);
        String message = requiredText(body, "message", 1000);
        Integer courseId = nullableInt(body.get("courseId"));
        return Map.of("ticketId", insertId("""
                INSERT INTO dbo.SupportTickets
                    (UserId, Name, Email, Category, CourseId, Subject, Message, Status)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'OPEN')
                """, user == null ? null : user.getId(), name, email, category, courseId, subject, message));
    }

    public Map<String, Object> fullCourse(int id) {
        return course(id);
    }

    public void addSection(int courseId, Map<String, Object> body) {
        requireExists("SELECT COUNT(*) FROM dbo.Courses WHERE CourseId = ?", courseId, "Course not found.");
        String title = requiredText(body, "title", 150);
        Integer parentId = nullableInt(body.get("parentSectionId"));
        if (parentId != null) {
            requireExists("SELECT COUNT(*) FROM dbo.Sections WHERE SectionId = ? AND CourseId = ?",
                    parentId, courseId, "Parent section not found.");
        }
        Integer position = jdbc.queryForObject("""
                SELECT COALESCE(MAX(Position), 0) + 1 FROM dbo.Sections
                WHERE CourseId = ? AND ((ParentSectionId IS NULL AND ? IS NULL) OR ParentSectionId = ?)
                """, Integer.class, courseId, parentId, parentId);
        jdbc.update("""
                INSERT INTO dbo.Sections (CourseId, ParentSectionId, Title, Position)
                VALUES (?, ?, ?, ?)
                """, courseId, parentId, title, position == null ? 1 : position);
    }

    @Transactional
    public void deleteSection(int sectionId) {
        requireExists("SELECT COUNT(*) FROM dbo.Sections WHERE SectionId = ?", sectionId, "Section not found.");
        List<Integer> ids = jdbc.queryForList("""
                WITH descendants AS (
                    SELECT SectionId, 0 AS Depth FROM dbo.Sections WHERE SectionId = ?
                    UNION ALL
                    SELECT s.SectionId, d.Depth + 1 FROM dbo.Sections s
                    JOIN descendants d ON s.ParentSectionId = d.SectionId
                )
                SELECT SectionId FROM descendants ORDER BY Depth DESC
                """, Integer.class, sectionId);
        for (int id : ids) {
            jdbc.update("DELETE FROM dbo.Sections WHERE SectionId = ?", id);
        }
    }

    public void addLesson(int sectionId, Map<String, Object> body) {
        requireExists("SELECT COUNT(*) FROM dbo.Sections WHERE SectionId = ?", sectionId, "Section not found.");
        String title = requiredText(body, "title", 150);
        String type = optionalText(body, "type", 10).toUpperCase(Locale.ROOT);
        if (type.isBlank()) {
            type = "TEXT";
        } else if (!Set.of("VIDEO", "TEXT", "FILE").contains(type)) {
            throw new AdminApiException(400, "Invalid lesson type.");
        }
        String contentUrl = optionalText(body, "contentUrl", 500);
        String text = optionalText(body, "text", 4000);
        int minutes = Math.max(0, number(body.get("minutes"), 0));
        boolean preview = Boolean.parseBoolean(String.valueOf(body.get("preview")));
        Integer position = jdbc.queryForObject("""
                SELECT COALESCE(MAX(Position), 0) + 1 FROM dbo.Lessons WHERE SectionId = ?
                """, Integer.class, sectionId);
        jdbc.update("""
                INSERT INTO dbo.Lessons
                    (SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, sectionId, title, type, contentUrl.isBlank() ? null : contentUrl,
                text.isBlank() ? null : text, minutes, preview, position == null ? 1 : position);
    }

    public void deleteLesson(int lessonId) {
        requireExists("SELECT COUNT(*) FROM dbo.Lessons WHERE LessonId = ?", lessonId, "Lesson not found.");
        jdbc.update("DELETE FROM dbo.Lessons WHERE LessonId = ?", lessonId);
    }

    @Transactional
    public Map<String, Object> saveQuiz(int sectionId, Map<String, Object> body) {
        requireExists("SELECT COUNT(*) FROM dbo.Sections WHERE SectionId = ?", sectionId, "Section not found.");
        String title = requiredText(body, "title", 150);
        int passScore = number(body.get("passScore"), 70);
        int maxAttempts = Math.max(0, number(body.get("maxAttempts"), 0));
        if (passScore < 1 || passScore > 100) {
            throw new AdminApiException(400, "Pass score must be between 1 and 100.");
        }

        List<?> questions = body.get("questions") instanceof List<?> list ? list : List.of();
        if (questions.isEmpty()) {
            throw new AdminApiException(400, "Add at least one quiz question.");
        }

        Integer quizId = jdbc.query("""
                SELECT QuizId FROM dbo.Quizzes WHERE SectionId = ?
                """, rs -> rs.next() ? rs.getInt(1) : null, sectionId);
        if (quizId == null) {
            quizId = insertId("""
                    INSERT INTO dbo.Quizzes (SectionId, Title, PassScore, MaxAttempts)
                    VALUES (?, ?, ?, ?)
                    """, sectionId, title, passScore, maxAttempts);
        } else {
            jdbc.update("""
                    UPDATE dbo.Quizzes SET Title = ?, PassScore = ?, MaxAttempts = ?
                    WHERE QuizId = ?
                    """, title, passScore, maxAttempts, quizId);
            jdbc.update("DELETE FROM dbo.Questions WHERE QuizId = ?", quizId);
        }

        int questionPosition = 1;
        for (Object value : questions) {
            if (!(value instanceof Map<?, ?> question)) {
                throw new AdminApiException(400, "Invalid quiz question.");
            }
            String text = requiredText(question, "text", 500);
            int questionId = insertId("""
                    INSERT INTO dbo.Questions (QuizId, QuestionText, Position)
                    VALUES (?, ?, ?)
                    """, quizId, text, questionPosition++);

            Object optionValue = question.get("options");
            List<?> options = optionValue instanceof List<?> list ? list : List.of();
            if (options.size() < 2 || options.size() > 4) {
                throw new AdminApiException(400, "Each question must have 2 to 4 options.");
            }
            int correctCount = 0;
            for (Object optionValueItem : options) {
                if (!(optionValueItem instanceof Map<?, ?> option)) {
                    throw new AdminApiException(400, "Invalid quiz option.");
                }
                String optionText = requiredText(option, "text", 300);
                boolean correct = Boolean.parseBoolean(String.valueOf(option.get("correct")));
                if (correct) correctCount++;
                jdbc.update("""
                        INSERT INTO dbo.Options (QuestionId, OptionText, IsCorrect)
                        VALUES (?, ?, ?)
                        """, questionId, optionText, correct);
            }
            if (correctCount != 1) {
                throw new AdminApiException(400, "Each question must have exactly one correct option.");
            }
        }
        return quiz(sectionId);
    }

    public void deleteQuiz(int sectionId) {
        jdbc.update("DELETE FROM dbo.Quizzes WHERE SectionId = ?", sectionId);
    }

    private List<Map<String, Object>> sections(int courseId) {
        List<Map<String, Object>> result = jdbc.queryForList("""
                SELECT SectionId AS sectionId, Title AS title, Position AS position
                FROM dbo.Sections WHERE CourseId = ?
                ORDER BY Position, SectionId
                """, courseId);
        for (Map<String, Object> section : result) {
            int sectionId = ((Number) section.get("sectionId")).intValue();
            section.put("lessons", jdbc.queryForList("""
                    SELECT LessonId AS lessonId, Title AS title, ContentType AS type,
                           ContentUrl AS contentUrl, TextContent AS text, Minutes AS minutes,
                           IsPreview AS preview, Position AS position
                    FROM dbo.Lessons WHERE SectionId = ?
                    ORDER BY Position, LessonId
                    """, sectionId));
            section.put("quiz", quizOrNull(sectionId));
        }
        return result;
    }

    private Map<String, Object> quizOrNull(int sectionId) {
        Integer quizId = jdbc.query("""
                SELECT QuizId FROM dbo.Quizzes WHERE SectionId = ?
                """, rs -> rs.next() ? rs.getInt(1) : null, sectionId);
        return quizId == null ? null : quiz(sectionId);
    }

    private Map<String, Object> quiz(int sectionId) {
        List<Map<String, Object>> quizzes = jdbc.queryForList("""
                SELECT QuizId AS quizId, Title AS title, PassScore AS passScore,
                       MaxAttempts AS maxAttempts
                FROM dbo.Quizzes WHERE SectionId = ?
                """, sectionId);
        if (quizzes.isEmpty()) {
            throw new AdminApiException(404, "Quiz not found.");
        }
        Map<String, Object> quiz = quizzes.get(0);
        int quizId = ((Number) quiz.get("quizId")).intValue();
        List<Map<String, Object>> questions = jdbc.queryForList("""
                SELECT QuestionId AS questionId, QuestionText AS text, Position AS position
                FROM dbo.Questions WHERE QuizId = ?
                ORDER BY Position, QuestionId
                """, quizId);
        for (Map<String, Object> question : questions) {
            int questionId = ((Number) question.get("questionId")).intValue();
            question.put("options", jdbc.queryForList("""
                    SELECT OptionText AS text, IsCorrect AS correct
                    FROM dbo.Options WHERE QuestionId = ? ORDER BY OptionId
                    """, questionId));
        }
        quiz.put("questions", questions);
        return quiz;
    }

    private CourseInput courseInput(Map<String, Object> body, int fallbackInstructorId, boolean update) {
        String title = requiredText(body, "title", 150);
        String description = optionalText(body, "description", 4000);
        int categoryId = number(body.get("categoryId"), 0);
        requireExists("SELECT COUNT(*) FROM dbo.Categories WHERE CategoryId = ?", categoryId, "Category not found.");
        String level = optionalText(body, "level", 15);
        if (level.isBlank()) {
            level = "Beginner";
        } else if (!Set.of("Beginner", "Intermediate", "Advanced").contains(level)) {
            throw new AdminApiException(400, "Invalid course level.");
        }
        BigDecimal price;
        try {
            price = new BigDecimal(String.valueOf(body.getOrDefault("price", "0")));
        } catch (NumberFormatException exception) {
            throw new AdminApiException(400, "Invalid course price.");
        }
        if (price.signum() < 0) {
            throw new AdminApiException(400, "Course price cannot be negative.");
        }
        String imageUrl = optionalText(body, "imageUrl", 500);
        String status = optionalText(body, "status", 15).toUpperCase(Locale.ROOT);
        if (status.isBlank()) status = update ? "DRAFT" : "PUBLISHED";
        if (!COURSE_STATUSES.contains(status)) {
            throw new AdminApiException(400, "Invalid course status.");
        }
        String rejectReason = optionalText(body, "rejectReason", 500);
        int instructorId = number(body.get("instructorId"), fallbackInstructorId);
        requireExists("SELECT COUNT(*) FROM dbo.users WHERE id = ?", instructorId, "Instructor not found.");
        User instructor = findUser(instructorId);
        String instructorRole = instructor.getRole() == null ? "" : instructor.getRole().getName();
        if (!"ADMIN".equalsIgnoreCase(instructorRole)
                && count("SELECT COUNT(*) FROM dbo.Courses WHERE InstructorId = ?", instructorId) == 0) {
            throw new AdminApiException(400, "Choose an admin or an existing course instructor.");
        }
        return new CourseInput(title, description.isBlank() ? null : description,
                categoryId, level, price, imageUrl.isBlank() ? null : imageUrl,
                status, rejectReason.isBlank() ? null : rejectReason, instructorId);
    }

    private Map<String, Object> instructor(Map<String, Object> course) {
        Map<String, Object> instructor = new LinkedHashMap<>();
        instructor.put("userId", course.get("instructorId"));
        instructor.put("fullName", course.get("instructorName"));
        return instructor;
    }

    private Map<String, Object> category(int id, String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("categoryId", id);
        result.put("name", name);
        return result;
    }

    private User findUser(int id) {
        return users.findById(id).orElseThrow(() -> new AdminApiException(404, "User not found."));
    }

    private void log(String type, String action, Integer userId, String email,
                     Integer courseId, String courseTitle, BigDecimal amount,
                     String method, String detail, String actor) {
        jdbc.update("""
                INSERT INTO dbo.ActivityLogs
                    (LogType, Action, UserId, Email, CourseId, CourseTitle,
                     Amount, PaymentMethod, Detail, ActorName)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, type, action, userId, email, courseId, courseTitle, amount, method, detail, actor);
    }

    private Map<String, Object> one(String sql, Object arg, String missingMessage) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, arg);
        if (rows.isEmpty()) {
            throw new AdminApiException(404, missingMessage);
        }
        return rows.get(0);
    }

    private boolean exists(String sql, Object... args) {
        Integer result = jdbc.queryForObject(sql, Integer.class, args);
        return result != null && result > 0;
    }

    private void requireExists(String sql, Object... argsAndMessage) {
        int messageIndex = argsAndMessage.length - 1;
        String message = String.valueOf(argsAndMessage[messageIndex]);
        Object[] args = new Object[messageIndex];
        System.arraycopy(argsAndMessage, 0, args, 0, messageIndex);
        if (!exists(sql, args)) {
            throw new AdminApiException(404, message);
        }
    }

    private int count(String sql, Object... args) {
        Integer result = jdbc.queryForObject(sql, Integer.class, args);
        return result == null ? 0 : result;
    }

    private int insertId(String sql, Object... args) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("The database did not return the generated identifier.");
        }
        return key.intValue();
    }

    private String requiredText(Map<?, ?> body, String key, int maxLength) {
        String value = optionalText(body, key, maxLength);
        if (value.isBlank()) {
            throw new AdminApiException(400, key + " is required.");
        }
        return value;
    }

    private String optionalText(Map<?, ?> body, String key, int maxLength) {
        Object value = body.get(key);
        String text = value == null ? "" : String.valueOf(value).trim();
        if (text.length() > maxLength) {
            throw new AdminApiException(400, key + " is too long.");
        }
        return text;
    }

    private int number(Object value, int fallback) {
        if (value == null || String.valueOf(value).isBlank()) return fallback;
        try {
            return new BigDecimal(String.valueOf(value)).intValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new AdminApiException(400, "Expected a whole number.");
        }
    }

    private Integer nullableInt(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        return number(value, 0);
    }

    private record CourseInput(String title, String description, int categoryId, String level,
                               BigDecimal price, String imageUrl, String status, String rejectReason,
                               int instructorId) {}
}
