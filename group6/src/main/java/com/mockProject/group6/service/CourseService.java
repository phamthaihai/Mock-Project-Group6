
package com.mockProject.group6.service;

import com.mockProject.group6.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Service
public class CourseService {

    private final JdbcTemplate jdbc;

    public CourseService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> categories() {
        return jdbc.queryForList("""
            SELECT CategoryId AS categoryId, Name AS name
            FROM dbo.Categories
            ORDER BY Name
            """);
    }

    public List<Map<String, Object>> catalog(String keyword, Integer categoryId) {
        StringBuilder sql = new StringBuilder("""
            SELECT c.CourseId AS courseId,
                   c.Title AS title,
                   c.Description AS description,
                   c.Price AS price,
                   c.ImageUrl AS imageUrl,
                   c.CourseLevel AS level,
                   cat.Name AS category,
                   u.fullname AS instructor,
                   COUNT(DISTINCT e.EnrollmentId) AS enrolledCount,
                   COUNT(DISTINCT l.LessonId) AS lessonCount
            FROM dbo.Courses c
            JOIN dbo.Categories cat ON cat.CategoryId = c.CategoryId
            JOIN dbo.users u ON u.id = c.InstructorId
            LEFT JOIN dbo.Enrollments e ON e.CourseId = c.CourseId
            LEFT JOIN dbo.Sections s ON s.CourseId = c.CourseId
            LEFT JOIN dbo.Lessons l ON l.SectionId = s.SectionId
            WHERE c.Status = 'PUBLISHED'
            """);

        List<Object> args = new ArrayList<>();

        if (categoryId != null) {
            sql.append(" AND c.CategoryId = ?");
            args.add(categoryId);
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (c.Title LIKE ? OR c.Description LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }

        sql.append("""
            GROUP BY c.CourseId, c.Title, c.Description, c.Price,
                     c.ImageUrl, c.CourseLevel, cat.Name, u.fullname, c.CreatedAt
            ORDER BY c.CreatedAt DESC
            """);

        return jdbc.queryForList(sql.toString(), args.toArray());
    }

    public Map<String, Object> course(int id) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT c.CourseId AS courseId,
                   c.Title AS title,
                   c.Description AS description,
                   c.CategoryId AS categoryId,
                   c.CourseLevel AS level,
                   c.Price AS price,
                   c.ImageUrl AS imageUrl,
                   c.Status AS status,
                   c.InstructorId AS instructorId,
                   cat.Name AS category,
                   u.fullname AS instructor
            FROM dbo.Courses c
            JOIN dbo.Categories cat ON cat.CategoryId = c.CategoryId
            JOIN dbo.users u ON u.id = c.InstructorId
            WHERE c.CourseId = ?
            """, id);

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Course not found.");
        }

        Map<String, Object> result = new HashMap<>(rows.get(0));
        result.put("sections", sectionsWithLessons(id));
        return result;
    }

    private List<Map<String, Object>> sectionsWithLessons(int courseId) {
        List<Map<String, Object>> sections = jdbc.queryForList("""
            SELECT SectionId AS sectionId,
                   Title AS title,
                   Position AS position
            FROM dbo.Sections
            WHERE CourseId = ?
            ORDER BY Position, SectionId
            """, courseId);

        for (Map<String, Object> section : sections) {
            section.put("lessons", jdbc.queryForList("""
                SELECT LessonId AS lessonId,
                       Title AS title,
                       ContentType AS type,
                       Minutes AS minutes,
                       IsPreview AS preview,
                       Position AS position
                FROM dbo.Lessons
                WHERE SectionId = ?
                ORDER BY Position, LessonId
                """, section.get("sectionId")));
        }

        return sections;
    }

    public boolean isEnrolled(int courseId, int userId) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM dbo.Enrollments
            WHERE CourseId = ? AND UserId = ?
            """, Integer.class, courseId, userId);

        return count != null && count > 0;
    }

    public List<Map<String, Object>> myEnrollments(int userId) {
        return jdbc.queryForList("""
            SELECT c.CourseId AS courseId,
                   c.Title AS title,
                   c.Description AS description,
                   c.ImageUrl AS imageUrl,
                   c.CourseLevel AS level,
                   cat.Name AS category,
                   u.fullname AS instructor,
                   COUNT(DISTINCT l.LessonId) AS totalLessons,
                   COUNT(DISTINCT lp.LessonId) AS completedLessons
            FROM dbo.Enrollments e
            JOIN dbo.Courses c ON c.CourseId = e.CourseId
            JOIN dbo.Categories cat ON cat.CategoryId = c.CategoryId
            JOIN dbo.users u ON u.id = c.InstructorId
            LEFT JOIN dbo.Sections s ON s.CourseId = c.CourseId
            LEFT JOIN dbo.Lessons l ON l.SectionId = s.SectionId
            LEFT JOIN dbo.LessonProgress lp
                   ON lp.LessonId = l.LessonId AND lp.UserId = ?
            WHERE e.UserId = ?
            GROUP BY c.CourseId, c.Title, c.Description, c.ImageUrl,
                     c.CourseLevel, cat.Name, u.fullname
            ORDER BY c.CourseId DESC
            """, userId, userId);
    }

    public List<Map<String, Object>> myCreatedCourses(int userId) {
        return jdbc.queryForList("""
            SELECT c.CourseId AS courseId,
                   c.Title AS title,
                   c.Description AS description,
                   c.ImageUrl AS imageUrl,
                   c.CourseLevel AS level,
                   cat.Name AS category,
                   COUNT(DISTINCT l.LessonId) AS lessonCount,
                   COUNT(DISTINCT lp.LessonId) AS completedLessons
            FROM dbo.Courses c
            JOIN dbo.Categories cat ON cat.CategoryId = c.CategoryId
            LEFT JOIN dbo.Sections s ON s.CourseId = c.CourseId
            LEFT JOIN dbo.Lessons l ON l.SectionId = s.SectionId
            LEFT JOIN dbo.LessonProgress lp
                   ON lp.LessonId = l.LessonId AND lp.UserId = ?
            WHERE c.InstructorId = ? AND c.Status = 'DRAFT'
            GROUP BY c.CourseId, c.Title, c.Description, c.ImageUrl,
                     c.CourseLevel, cat.Name
            ORDER BY c.CourseId DESC
            """, userId, userId);
    }

    @Transactional
    public int createPersonalCourse(
            User user,
            String title,
            String description,
            int categoryId,
            String level,
            BigDecimal price,
            String imageUrl,
            String lessonLines) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Course title is required.");
        }

        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Invalid course price.");
        }

        Integer categoryCount = jdbc.queryForObject("""
            SELECT COUNT(*) FROM dbo.Categories WHERE CategoryId = ?
            """, Integer.class, categoryId);

        if (categoryCount == null || categoryCount == 0) {
            throw new IllegalArgumentException("Invalid category.");
        }

        KeyHolder holder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO dbo.Courses
                    (Title, Description, CategoryId, CourseLevel,
                     Price, ImageUrl, Status, InstructorId)
                VALUES (?, ?, ?, ?, ?, ?, 'DRAFT', ?)
                """, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, title.trim());
            ps.setString(2, description);
            ps.setInt(3, categoryId);
            ps.setString(4, level);
            ps.setBigDecimal(5, price);
            ps.setString(6, imageUrl);
            ps.setInt(7, user.getId());
            return ps;
        }, holder);

        int courseId = Objects.requireNonNull(holder.getKey()).intValue();

        if (lessonLines != null && !lessonLines.isBlank()) {
            int sectionId = addSectionInternal(courseId, null, "Getting Started");

            for (String line : lessonLines.split("\\R")) {
                if (!line.isBlank()) {
                    addLessonInternal(sectionId, line.trim(), null,
                            null, "TEXT", 5);
                }
            }
        }

        return courseId;
    }

    @Transactional
    public void enroll(int courseId, int userId) {
        Map<String, Object> c = course(courseId);

        if (!"PUBLISHED".equals(c.get("status"))) {
            throw new IllegalArgumentException("This course is not available.");
        }

        if (((Number) c.get("instructorId")).intValue() == userId) {
            throw new IllegalArgumentException("You cannot enroll in your own course.");
        }

        if (isEnrolled(courseId, userId)) {
            throw new IllegalArgumentException("You are already enrolled.");
        }

        BigDecimal price = (BigDecimal) c.get("price");

        if (price.signum() > 0) {
            throw new IllegalArgumentException("Please purchase this course.");
        }

        jdbc.update("""
            INSERT INTO dbo.Enrollments
                (CourseId, UserId, PricePaid, PaymentMethod)
            VALUES (?, ?, 0, 'FREE')
            """, courseId, userId);
    }


public List<Map<String, Object>> cart(int userId) {
    return jdbc.queryForList("""
        SELECT c.CourseId AS courseId,
               c.Title AS title,
               c.Price AS price,
               c.ImageUrl AS imageUrl,
               cat.Name AS category
        FROM dbo.CartItems ci
        JOIN dbo.Courses c ON c.CourseId = ci.CourseId
        LEFT JOIN dbo.Categories cat
            ON cat.CategoryId = c.CategoryId
        WHERE ci.UserId = ?
        ORDER BY ci.AddedAt DESC
        """, userId);
}


    public void addToCart(int courseId, int userId) {
        Map<String, Object> c = course(courseId);

        if (!"PUBLISHED".equals(c.get("status"))) {
            throw new IllegalArgumentException("This course is unavailable.");
        }

        if (((Number) c.get("instructorId")).intValue() == userId) {
            throw new IllegalArgumentException("You cannot purchase your own course.");
        }

        if (isEnrolled(courseId, userId)) {
            throw new IllegalArgumentException("You already own this course.");
        }

        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM dbo.CartItems
            WHERE UserId = ? AND CourseId = ?
            """, Integer.class, userId, courseId);

        if (count != null && count > 0) {
            throw new IllegalArgumentException("Course is already in your cart.");
        }

        jdbc.update("""
            INSERT INTO dbo.CartItems (UserId, CourseId)
            VALUES (?, ?)
            """, userId, courseId);
    }

    public void removeFromCart(int courseId, int userId) {
        jdbc.update("""
            DELETE FROM dbo.CartItems
            WHERE UserId = ? AND CourseId = ?
            """, userId, courseId);
    }

    @Transactional
    public Map<String, Object> checkout(int userId, String paymentMethod) {
        Set<String> methods = Set.of("CARD", "EWALLET", "BANK", "DEMO");

        if (paymentMethod == null ||
                !methods.contains(paymentMethod.toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid payment method.");
        }

        List<Map<String, Object>> items = cart(userId);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (Map<String, Object> item : items) {
            int courseId = ((Number) item.get("courseId")).intValue();
            Map<String, Object> c = course(courseId);

            if (!"PUBLISHED".equals(c.get("status"))) {
                throw new IllegalArgumentException("A course is no longer available.");
            }

            if (((Number) c.get("instructorId")).intValue() == userId) {
                throw new IllegalArgumentException("You cannot purchase your own course.");
            }

            if (isEnrolled(courseId, userId)) {
                throw new IllegalArgumentException("You already own a course in your cart.");
            }

            BigDecimal price = (BigDecimal) c.get("price");
            total = total.add(price);

            jdbc.update("""
                INSERT INTO dbo.Enrollments
                    (CourseId, UserId, PricePaid, PaymentMethod)
                VALUES (?, ?, ?, ?)
                """, courseId, userId, price,
                    paymentMethod.toUpperCase(Locale.ROOT));
        }

        jdbc.update("DELETE FROM dbo.CartItems WHERE UserId = ?", userId);

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("paymentMethod", paymentMethod.toUpperCase(Locale.ROOT));
        result.put("orderNumber", "ST-" + System.currentTimeMillis());

        return result;
    }

    public boolean isPersonalOwner(int courseId, int userId) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM dbo.Courses
            WHERE CourseId = ?
              AND InstructorId = ?
              AND Status = 'DRAFT'
            """, Integer.class, courseId, userId);

        return count != null && count > 0;
    }

    public void updatePersonalCourse(
            int courseId, int userId, String title, String description) {

        requireOwner(courseId, userId);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Course title is required.");
        }

        jdbc.update("""
            UPDATE dbo.Courses
            SET Title = ?, Description = ?
            WHERE CourseId = ?
            """, title.trim(), description, courseId);
    }

    public void deletePersonalCourse(int courseId, int userId) {
        requireOwner(courseId, userId);

        jdbc.update("""
            DELETE FROM dbo.Courses WHERE CourseId = ?
            """, courseId);
    }

    public void addPersonalLesson(
            int courseId, int userId, String title, String content) {

        requireOwner(courseId, userId);

        List<Map<String, Object>> sections = sectionsFlat(courseId);
        int sectionId;

        if (sections.isEmpty()) {
            sectionId = addSectionInternal(courseId, null, "Getting Started");
        } else {
            sectionId = ((Number) sections.get(0).get("sectionId")).intValue();
        }

        addLessonInternal(sectionId, title, content, null, "TEXT", 5);
    }

    public List<Map<String, Object>> sectionsFlat(int courseId) {
        return jdbc.queryForList("""
            SELECT SectionId AS sectionId,
                   ParentSectionId AS parentSectionId,
                   Title AS title,
                   Position AS position
            FROM dbo.Sections
            WHERE CourseId = ?
            ORDER BY Position, SectionId
            """, courseId);
    }

    public List<Map<String, Object>> lessons(int courseId) {
        return jdbc.queryForList("""
            SELECT l.LessonId AS lessonId,
                   l.SectionId AS sectionId,
                   l.Title AS title,
                   l.ContentType AS type,
                   l.ContentUrl AS contentUrl,
                   l.TextContent AS text,
                   l.Minutes AS minutes,
                   l.Position AS position,
                   s.Title AS sectionTitle
            FROM dbo.Lessons l
            JOIN dbo.Sections s ON s.SectionId = l.SectionId
            WHERE s.CourseId = ?
            ORDER BY s.Position, s.SectionId, l.Position, l.LessonId
            """, courseId);
    }

    public Map<String, Object> lesson(int courseId, int lessonId) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT l.LessonId AS lessonId,
                   l.SectionId AS sectionId,
                   l.Title AS title,
                   l.ContentType AS type,
                   l.ContentUrl AS contentUrl,
                   l.TextContent AS text,
                   l.Minutes AS minutes,
                   l.Position AS position,
                   s.Title AS sectionTitle
            FROM dbo.Lessons l
            JOIN dbo.Sections s ON s.SectionId = l.SectionId
            WHERE s.CourseId = ? AND l.LessonId = ?
            """, courseId, lessonId);

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Lesson not found.");
        }

        return rows.get(0);
    }

    public Set<Integer> completedLessonIds(int courseId, int userId) {
        List<Integer> ids = jdbc.queryForList("""
            SELECT lp.LessonId
            FROM dbo.LessonProgress lp
            JOIN dbo.Lessons l ON l.LessonId = lp.LessonId
            JOIN dbo.Sections s ON s.SectionId = l.SectionId
            WHERE s.CourseId = ? AND lp.UserId = ?
            """, Integer.class, courseId, userId);

        return new HashSet<>(ids);
    }

    @Transactional
    public void markLesson(
            int courseId, int lessonId, int userId, boolean done) {

        if (!isEnrolled(courseId, userId)
                && !isPersonalOwner(courseId, userId)) {
            throw new IllegalArgumentException("You cannot access this lesson.");
        }

        lesson(courseId, lessonId);

        if (done) {
            jdbc.update("""
                IF NOT EXISTS (
                    SELECT 1 FROM dbo.LessonProgress
                    WHERE UserId = ? AND LessonId = ?
                )
                INSERT INTO dbo.LessonProgress (UserId, LessonId)
                VALUES (?, ?)
                """, userId, lessonId, userId, lessonId);
        } else {
            jdbc.update("""
                DELETE FROM dbo.LessonProgress
                WHERE UserId = ? AND LessonId = ?
                """, userId, lessonId);
        }
    }

    private void requireOwner(int courseId, int userId) {
        if (!isPersonalOwner(courseId, userId)) {
            throw new IllegalArgumentException(
                    "You can only edit your own private courses."
            );
        }
    }

    private int addSectionInternal(
            int courseId, Integer parentId, String title) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Module title is required.");
        }

        Integer position = jdbc.queryForObject("""
            SELECT COALESCE(MAX(Position), 0) + 1
            FROM dbo.Sections
            WHERE CourseId = ?
              AND (
                  (ParentSectionId IS NULL AND ? IS NULL)
                  OR ParentSectionId = ?
              )
            """, Integer.class, courseId, parentId, parentId);

        KeyHolder holder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO dbo.Sections
                    (CourseId, ParentSectionId, Title, Position)
                VALUES (?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, courseId);

            if (parentId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, parentId);
            }

            ps.setString(3, title.trim());
            ps.setInt(4, position == null ? 1 : position);

            return ps;
        }, holder);

        return Objects.requireNonNull(holder.getKey()).intValue();
    }

    public int addSection(
            int courseId, int userId, Integer parentId, String title) {

        requireOwner(courseId, userId);

        if (parentId != null) {
            Integer count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.Sections
                WHERE SectionId = ? AND CourseId = ?
                """, Integer.class, parentId, courseId);

            if (count == null || count == 0) {
                throw new IllegalArgumentException("Parent module not found.");
            }
        }

        return addSectionInternal(courseId, parentId, title);
    }

    public void renameSection(
            int courseId, int userId, int sectionId, String title) {

        requireOwner(courseId, userId);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Module title is required.");
        }

        int updated = jdbc.update("""
            UPDATE dbo.Sections
            SET Title = ?
            WHERE SectionId = ? AND CourseId = ?
            """, title.trim(), sectionId, courseId);

        if (updated == 0) {
            throw new IllegalArgumentException("Module not found.");
        }
    }

    @Transactional
    public void deleteSection(
            int courseId, int userId, int sectionId) {

        requireOwner(courseId, userId);

        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM dbo.Sections
            WHERE SectionId = ? AND CourseId = ?
            """, Integer.class, sectionId, courseId);

        if (count == null || count == 0) {
            throw new IllegalArgumentException("Module not found.");
        }

        List<Map<String, Object>> sections = sectionsFlat(courseId);
        Set<Integer> toDelete = new HashSet<>();
        toDelete.add(sectionId);

        boolean changed;

        do {
            changed = false;

            for (Map<String, Object> section : sections) {
                Object parent = section.get("parentSectionId");
                int id = ((Number) section.get("sectionId")).intValue();

                if (parent != null
                        && toDelete.contains(((Number) parent).intValue())
                        && toDelete.add(id)) {
                    changed = true;
                }
            }
        } while (changed);

        List<Integer> ids = new ArrayList<>(toDelete);
        Collections.reverse(ids);

        for (int id : ids) {
            jdbc.update("""
                DELETE FROM dbo.Sections
                WHERE SectionId = ? AND CourseId = ?
                """, id, courseId);
        }
    }

    private int addLessonInternal(
            int sectionId,
            String title,
            String content,
            String contentUrl,
            String type,
            int minutes) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Lesson title is required.");
        }

        String normalizedType = normalizeType(type);

        Integer position = jdbc.queryForObject("""
            SELECT COALESCE(MAX(Position), 0) + 1
            FROM dbo.Lessons
            WHERE SectionId = ?
            """, Integer.class, sectionId);

        KeyHolder holder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO dbo.Lessons
                    (SectionId, Title, ContentType, ContentUrl,
                     TextContent, Minutes, IsPreview, Position)
                VALUES (?, ?, ?, ?, ?, ?, 0, ?)
                """, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, sectionId);
            ps.setString(2, title.trim());
            ps.setString(3, normalizedType);
            ps.setString(4, contentUrl);
            ps.setString(5, content);
            ps.setInt(6, Math.max(0, minutes));
            ps.setInt(7, position == null ? 1 : position);

            return ps;
        }, holder);

        return Objects.requireNonNull(holder.getKey()).intValue();
    }

    public int addLessonToSection(
            int courseId,
            int userId,
            int sectionId,
            String title,
            String content,
            String contentUrl,
            String type,
            int minutes) {

        requireOwner(courseId, userId);
        requireSection(courseId, sectionId);

        return addLessonInternal(
                sectionId, title, content, contentUrl, type, minutes
        );
    }

    public void updateLesson(
            int courseId,
            int userId,
            int lessonId,
            int sectionId,
            String title,
            String content,
            String contentUrl,
            String type,
            int minutes) {

        requireOwner(courseId, userId);
        lesson(courseId, lessonId);
        requireSection(courseId, sectionId);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Lesson title is required.");
        }

        jdbc.update("""
            UPDATE dbo.Lessons
            SET SectionId = ?,
                Title = ?,
                TextContent = ?,
                ContentUrl = ?,
                ContentType = ?,
                Minutes = ?
            WHERE LessonId = ?
            """, sectionId, title.trim(), content, contentUrl,
                normalizeType(type), Math.max(0, minutes), lessonId);
    }

    public void deleteLesson(int courseId, int userId, int lessonId) {
        requireOwner(courseId, userId);
        lesson(courseId, lessonId);

        jdbc.update("""
            DELETE FROM dbo.Lessons
            WHERE LessonId = ?
            """, lessonId);
    }

    private void requireSection(int courseId, int sectionId) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM dbo.Sections
            WHERE CourseId = ? AND SectionId = ?
            """, Integer.class, courseId, sectionId);

        if (count == null || count == 0) {
            throw new IllegalArgumentException("Module not found.");
        }
    }

    private String normalizeType(String type) {
        String value = type == null
                ? "TEXT"
                : type.trim().toUpperCase(Locale.ROOT);

        if (!Set.of("TEXT", "VIDEO", "FILE").contains(value)) {
            throw new IllegalArgumentException("Invalid lesson type.");
        }

        return value;
    }

    public List<Map<String, Object>> outline(int courseId, int userId) {
        List<Map<String, Object>> sections = sectionsFlat(courseId);
        List<Map<String, Object>> allLessons = lessons(courseId);
        Set<Integer> completed = completedLessonIds(courseId, userId);

        Map<Integer, Map<String, Object>> nodes = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();

        for (Map<String, Object> section : sections) {
            int sectionId = ((Number) section.get("sectionId")).intValue();

            Map<String, Object> node = new HashMap<>();
            node.put("sectionId", sectionId);
            node.put("title", section.get("title"));
            node.put("parentSectionId", section.get("parentSectionId"));
            node.put("position", section.get("position"));
            node.put("children", new ArrayList<Map<String, Object>>());
            node.put("lessons", new ArrayList<Map<String, Object>>());
            node.put("total", 0);
            node.put("done", 0);
            node.put("percent", 0);

            nodes.put(sectionId, node);
        }

        for (Map<String, Object> lesson : allLessons) {
            int sectionId = ((Number) lesson.get("sectionId")).intValue();
            Map<String, Object> node = nodes.get(sectionId);

            if (node != null) {
                Map<String, Object> item = new HashMap<>(lesson);
                item.put("done",
                        completed.contains(((Number) lesson.get("lessonId")).intValue()));

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> nodeLessons =
                        (List<Map<String, Object>>) node.get("lessons");

                nodeLessons.add(item);
            }
        }

        for (Map<String, Object> node : nodes.values()) {
            Object parentValue = node.get("parentSectionId");

            if (parentValue == null) {
                roots.add(node);
            } else {
                int parentId = ((Number) parentValue).intValue();
                Map<String, Object> parent = nodes.get(parentId);

                if (parent == null) {
                    roots.add(node);
                } else {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children =
                            (List<Map<String, Object>>) parent.get("children");

                    children.add(node);
                }
            }
        }

        for (Map<String, Object> root : roots) {
            calculateProgress(root);
        }

        return roots;
    }

    @SuppressWarnings("unchecked")
    private int[] calculateProgress(Map<String, Object> node) {
        int total = 0;
        int done = 0;

        List<Map<String, Object>> nodeLessons =
                (List<Map<String, Object>>) node.get("lessons");

        for (Map<String, Object> lesson : nodeLessons) {
            total++;

            if (Boolean.TRUE.equals(lesson.get("done"))) {
                done++;
            }
        }

        List<Map<String, Object>> children =
                (List<Map<String, Object>>) node.get("children");

        for (Map<String, Object> child : children) {
            int[] progress = calculateProgress(child);
            total += progress[0];
            done += progress[1];
        }

        node.put("total", total);
        node.put("done", done);
        node.put("percent",
                total == 0 ? 0 : (int) Math.round(done * 100.0 / total));

        return new int[]{total, done};
    }
}
