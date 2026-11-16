package com.mockProject.group6.service;

import com.mockProject.group6.exception.ApiException;
import com.mockProject.group6.security.AuthUser;
import com.mockProject.group6.support.In;
import com.mockProject.group6.support.Json;
import com.mockProject.group6.support.Rows;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Catalog, course outline, learner progress and (admin) course management. */
@Service
public class CourseService {

    private final JdbcTemplate jdbc;
    private final ActivityService act;

    public CourseService(JdbcTemplate jdbc, ActivityService act) {
        this.jdbc = jdbc;
        this.act = act;
    }

    /* ------------------------------------------------------------------ small helpers */

    static String ids(Collection<Integer> c) {
        if (c.isEmpty()) return "NULL";
        StringBuilder sb = new StringBuilder();
        for (Integer i : c) {
            if (sb.length() > 0) sb.append(',');
            sb.append(i.intValue());
        }
        return sb.toString();
    }

    private static Map<Integer, List<Map<String, Object>>> group(List<Map<String, Object>> rows, String key) {
        Map<Integer, List<Map<String, Object>>> m = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) m.computeIfAbsent(Rows.i(r, key), k -> new ArrayList<>()).add(r);
        return m;
    }

    private static Map<Integer, Map<String, Object>> byKey(List<Map<String, Object>> rows, String key) {
        Map<Integer, Map<String, Object>> m = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) m.put(Rows.i(r, key), r);
        return m;
    }

    private boolean exists(String sql, Object... args) {
        return !jdbc.queryForList(sql, args).isEmpty();
    }

    private int insert(String sql, Object... args) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int k = 0; k < args.length; k++) ps.setObject(k + 1, args[k]);
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key == null) throw new IllegalStateException("No generated key");
        return key.intValue();
    }

    private static void need(boolean ok, String message) {
        if (!ok) throw new ApiException(400, message);
    }

    /** The raw Courses row. 404 when it does not exist. */
    public Map<String, Object> row(int courseId) {
        List<Map<String, Object>> l = jdbc.queryForList("SELECT * FROM Courses WHERE CourseId = ?", courseId);
        if (l.isEmpty()) throw new ApiException(404, "Course not found");
        return l.get(0);
    }

    /** Course owner or admin may see a course that is not published. */
    public boolean canSeeUnpublished(AuthUser me, Map<String, Object> course) {
        return me != null && (me.admin() || Rows.i(course, "InstructorId") == me.userId());
    }

    public boolean enrolled(int courseId, Integer userId) {
        return userId != null && exists("SELECT 1 FROM Enrollments WHERE CourseId = ? AND UserId = ?", courseId, userId);
    }

    /* ------------------------------------------------------------------ catalog views */

    /** Courses matching "WHERE ..." (use ? for values) as the Course JSON of API.md, sections without lesson content. */
    public List<Map<String, Object>> views(String where, Object... args) {
        List<Map<String, Object>> cs = jdbc.queryForList(
                "SELECT c.CourseId, c.Title, c.Description, c.CategoryId, k.Name AS CategoryName, c.CourseLevel, c.Price, c.ImageUrl, "
                        + "c.Status, c.RejectReason, c.InstructorId, u.FullName AS InstructorName, c.Details "
                        + "FROM Courses c JOIN Categories k ON k.CategoryId = c.CategoryId JOIN Users u ON u.UserId = c.InstructorId "
                        + where + " ORDER BY c.CourseId", args);
        List<Map<String, Object>> out = new ArrayList<>();
        if (cs.isEmpty()) return out;
        List<Integer> courseIds = new ArrayList<>();
        for (Map<String, Object> c : cs) courseIds.add(Rows.i(c, "CourseId"));
        String in = ids(courseIds);

        Map<Integer, List<Map<String, Object>>> sections = group(jdbc.queryForList(
                "SELECT SectionId, CourseId, Title, Position FROM Sections WHERE CourseId IN (" + in + ") ORDER BY CourseId, Position, SectionId"), "CourseId");
        Map<Integer, List<Map<String, Object>>> lessons = group(jdbc.queryForList(
                "SELECT l.LessonId, l.SectionId, l.Title, l.ContentType, l.Minutes, l.IsPreview, l.Position FROM Lessons l "
                        + "JOIN Sections s ON s.SectionId = l.SectionId WHERE s.CourseId IN (" + in + ") ORDER BY l.SectionId, l.Position, l.LessonId"), "SectionId");
        Map<Integer, Map<String, Object>> quizzes = byKey(jdbc.queryForList(
                "SELECT q.QuizId, q.SectionId, q.Title, q.PassScore, q.MaxAttempts, (SELECT COUNT(*) FROM Questions x WHERE x.QuizId = q.QuizId) AS QCount "
                        + "FROM Quizzes q JOIN Sections s ON s.SectionId = q.SectionId WHERE s.CourseId IN (" + in + ")"), "SectionId");
        Map<Integer, Map<String, Object>> ratings = byKey(jdbc.queryForList(
                "SELECT CourseId, AVG(CAST(Rating AS FLOAT)) AS Avg, COUNT(*) AS Cnt FROM Reviews WHERE CourseId IN (" + in + ") GROUP BY CourseId"), "CourseId");
        Map<Integer, Map<String, Object>> enrolledCounts = byKey(jdbc.queryForList(
                "SELECT CourseId, COUNT(*) AS Cnt FROM Enrollments WHERE CourseId IN (" + in + ") GROUP BY CourseId"), "CourseId");

        for (Map<String, Object> c : cs) {
            int id = Rows.i(c, "CourseId");
            int lessonCount = 0, minutes = 0;
            List<Map<String, Object>> sectionJson = new ArrayList<>();
            for (Map<String, Object> s : sections.getOrDefault(id, new ArrayList<>())) {
                int sid = Rows.i(s, "SectionId");
                List<Map<String, Object>> lessonJson = new ArrayList<>();
                for (Map<String, Object> l : lessons.getOrDefault(sid, new ArrayList<>())) {
                    Map<String, Object> lj = new LinkedHashMap<>();
                    lj.put("lessonId", Rows.i(l, "LessonId"));
                    lj.put("title", Rows.s(l, "Title"));
                    lj.put("type", Rows.s(l, "ContentType"));
                    lj.put("minutes", Rows.i(l, "Minutes"));
                    lj.put("preview", Rows.b(l, "IsPreview"));
                    lj.put("position", Rows.i(l, "Position"));
                    lessonJson.add(lj);
                    lessonCount++;
                    minutes += Rows.i(l, "Minutes");
                }
                Map<String, Object> sj = new LinkedHashMap<>();
                sj.put("sectionId", sid);
                sj.put("title", Rows.s(s, "Title"));
                sj.put("position", Rows.i(s, "Position"));
                sj.put("lessons", lessonJson);
                Map<String, Object> q = quizzes.get(sid);
                if (q == null) {
                    sj.put("quiz", null);
                } else {
                    Map<String, Object> qj = new LinkedHashMap<>();
                    qj.put("quizId", Rows.i(q, "QuizId"));
                    qj.put("title", Rows.s(q, "Title"));
                    qj.put("passScore", Rows.i(q, "PassScore"));
                    qj.put("maxAttempts", Rows.i(q, "MaxAttempts"));
                    qj.put("questionCount", Rows.i(q, "QCount"));
                    sj.put("quiz", qj);
                }
                sectionJson.add(sj);
            }
            Map<String, Object> r = ratings.get(id);
            int ratingCount = r == null ? 0 : Rows.i(r, "Cnt");
            double rating = r == null ? 0 : Math.round(Rows.d(r, "Avg") * 10) / 10.0;
            Map<String, Object> inst = new LinkedHashMap<>();
            inst.put("userId", Rows.i(c, "InstructorId"));
            inst.put("fullName", Rows.sn(c, "InstructorName"));

            Map<String, Object> v = new LinkedHashMap<>();
            v.put("courseId", id);
            v.put("title", Rows.s(c, "Title"));
            v.put("description", Rows.sn(c, "Description"));
            v.put("categoryId", Rows.i(c, "CategoryId"));
            v.put("category", Rows.sn(c, "CategoryName"));
            v.put("level", Rows.s(c, "CourseLevel"));
            v.put("price", Rows.d(c, "Price"));
            v.put("imageUrl", Rows.s(c, "ImageUrl"));
            v.put("status", Rows.s(c, "Status"));
            v.put("rejectReason", Rows.s(c, "RejectReason"));
            v.put("instructor", inst);
            v.put("language", "English");
            v.put("durationHours", Math.max(1, (int) Math.ceil(minutes / 60.0)));
            v.put("details", Json.parseMap(Rows.s(c, "Details")));
            v.put("rating", rating);
            v.put("ratingCount", ratingCount);
            v.put("enrolledCount", enrolledCounts.containsKey(id) ? Rows.i(enrolledCounts.get(id), "Cnt") : 0);
            v.put("lessonCount", lessonCount);
            v.put("sections", sectionJson);
            out.add(v);
        }
        return out;
    }

    /** One course as JSON; null when it does not exist. */
    public Map<String, Object> view(int courseId) {
        List<Map<String, Object>> l = views("WHERE c.CourseId = ?", courseId);
        return l.isEmpty() ? null : l.get(0);
    }

    public Map<String, Object> viewOrFail(int courseId) {
        Map<String, Object> v = view(courseId);
        if (v == null) throw new ApiException(404, "Course not found");
        return v;
    }

    /** Sections with full lesson content and quiz questions including the correct flags (admin editor only). */
    public List<Map<String, Object>> fullSections(int courseId) {
        List<Map<String, Object>> secs = jdbc.queryForList(
                "SELECT SectionId, Title, Position FROM Sections WHERE CourseId = ? ORDER BY Position, SectionId", courseId);
        Map<Integer, List<Map<String, Object>>> lessons = group(jdbc.queryForList(
                "SELECT l.LessonId, l.SectionId, l.Title, l.ContentType, l.ContentUrl, l.TextContent, l.Minutes, l.IsPreview, l.Position FROM Lessons l "
                        + "JOIN Sections s ON s.SectionId = l.SectionId WHERE s.CourseId = ? ORDER BY l.SectionId, l.Position, l.LessonId", courseId), "SectionId");
        Map<Integer, Map<String, Object>> quizzes = byKey(jdbc.queryForList(
                "SELECT q.QuizId, q.SectionId, q.Title, q.PassScore, q.MaxAttempts FROM Quizzes q JOIN Sections s ON s.SectionId = q.SectionId WHERE s.CourseId = ?", courseId), "SectionId");
        List<Integer> quizIds = new ArrayList<>();
        for (Map<String, Object> q : quizzes.values()) quizIds.add(Rows.i(q, "QuizId"));
        Map<Integer, List<Map<String, Object>>> questions = group(jdbc.queryForList(
                "SELECT QuestionId, QuizId, QuestionText, Position FROM Questions WHERE QuizId IN (" + ids(quizIds) + ") ORDER BY QuizId, Position, QuestionId"), "QuizId");
        List<Integer> questionIds = new ArrayList<>();
        for (List<Map<String, Object>> ql : questions.values()) for (Map<String, Object> q : ql) questionIds.add(Rows.i(q, "QuestionId"));
        Map<Integer, List<Map<String, Object>>> options = group(jdbc.queryForList(
                "SELECT OptionId, QuestionId, OptionText, IsCorrect FROM Options WHERE QuestionId IN (" + ids(questionIds) + ") ORDER BY QuestionId, OptionId"), "QuestionId");

        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> s : secs) {
            int sid = Rows.i(s, "SectionId");
            Map<String, Object> sj = new LinkedHashMap<>();
            sj.put("sectionId", sid);
            sj.put("title", Rows.s(s, "Title"));
            sj.put("position", Rows.i(s, "Position"));
            List<Map<String, Object>> lj = new ArrayList<>();
            for (Map<String, Object> l : lessons.getOrDefault(sid, new ArrayList<>())) lj.add(lessonJson(l));
            sj.put("lessons", lj);
            Map<String, Object> q = quizzes.get(sid);
            if (q == null) {
                sj.put("quiz", null);
            } else {
                Map<String, Object> qj = new LinkedHashMap<>();
                qj.put("quizId", Rows.i(q, "QuizId"));
                qj.put("title", Rows.s(q, "Title"));
                qj.put("passScore", Rows.i(q, "PassScore"));
                qj.put("maxAttempts", Rows.i(q, "MaxAttempts"));
                List<Map<String, Object>> qs = new ArrayList<>();
                for (Map<String, Object> x : questions.getOrDefault(Rows.i(q, "QuizId"), new ArrayList<>())) {
                    Map<String, Object> xj = new LinkedHashMap<>();
                    xj.put("questionId", Rows.i(x, "QuestionId"));
                    xj.put("text", Rows.s(x, "QuestionText"));
                    List<Map<String, Object>> os = new ArrayList<>();
                    for (Map<String, Object> o : options.getOrDefault(Rows.i(x, "QuestionId"), new ArrayList<>())) {
                        Map<String, Object> oj = new LinkedHashMap<>();
                        oj.put("optionId", Rows.i(o, "OptionId"));
                        oj.put("text", Rows.s(o, "OptionText"));
                        oj.put("correct", Rows.b(o, "IsCorrect"));
                        os.add(oj);
                    }
                    xj.put("options", os);
                    qs.add(xj);
                }
                qj.put("questions", qs);
                sj.put("quiz", qj);
            }
            out.add(sj);
        }
        return out;
    }

    /** GET /instructor/courses/{id}: the course view with full sections. */
    public Map<String, Object> fullCourse(int courseId) {
        Map<String, Object> v = viewOrFail(courseId);
        v.put("sections", fullSections(courseId));
        return v;
    }

    private static Map<String, Object> lessonJson(Map<String, Object> l) {
        Map<String, Object> lj = new LinkedHashMap<>();
        lj.put("lessonId", Rows.i(l, "LessonId"));
        lj.put("title", Rows.s(l, "Title"));
        lj.put("type", Rows.s(l, "ContentType"));
        lj.put("contentUrl", Rows.s(l, "ContentUrl"));
        lj.put("text", Rows.sn(l, "TextContent"));
        lj.put("minutes", Rows.i(l, "Minutes"));
        lj.put("preview", Rows.b(l, "IsPreview"));
        lj.put("position", Rows.i(l, "Position"));
        return lj;
    }

    /* ------------------------------------------------------------------ lesson + progress */

    /** GET /lessons/{id}: preview lessons for anyone, the rest for enrolled students, the owner and admins. */
    public Map<String, Object> lesson(int lessonId, AuthUser me) {
        List<Map<String, Object>> l = jdbc.queryForList(
                "SELECT l.LessonId, l.Title, l.ContentType, l.ContentUrl, l.TextContent, l.Minutes, l.IsPreview, l.Position, s.CourseId, c.Status, c.InstructorId "
                        + "FROM Lessons l JOIN Sections s ON s.SectionId = l.SectionId JOIN Courses c ON c.CourseId = s.CourseId WHERE l.LessonId = ?", lessonId);
        if (l.isEmpty()) throw new ApiException(404, "Lesson not found");
        Map<String, Object> r = l.get(0);
        boolean owner = me != null && Rows.i(r, "InstructorId") == me.userId();
        if (!"PUBLISHED".equals(Rows.s(r, "Status")) && !(me != null && (me.admin() || owner))) throw new ApiException(404, "Lesson not found");
        if (!Rows.b(r, "IsPreview")) {
            if (me == null) throw new ApiException(401, "Please log in first");
            if (!(me.admin() || owner || enrolled(Rows.i(r, "CourseId"), me.userId()))) throw new ApiException(403, "Enroll in this course to view this lesson");
        }
        Map<String, Object> out = lessonJson(r);
        out.put("courseId", Rows.i(r, "CourseId"));
        return out;
    }

    /** GET /courses/{id}/progress (guests: enrolled=false). */
    public Map<String, Object> progress(int courseId, Integer userId) {
        boolean en = enrolled(courseId, userId);
        List<Integer> ls = jdbc.queryForList(
                "SELECT l.LessonId FROM Lessons l JOIN Sections s ON s.SectionId = l.SectionId WHERE s.CourseId = ? ORDER BY s.Position, s.SectionId, l.Position, l.LessonId",
                Integer.class, courseId);
        List<Integer> done = new ArrayList<>();
        if (en) {
            List<Integer> p = jdbc.queryForList(
                    "SELECT p.LessonId FROM LessonProgress p JOIN Lessons l ON l.LessonId = p.LessonId JOIN Sections s ON s.SectionId = l.SectionId "
                            + "WHERE p.UserId = ? AND s.CourseId = ?", Integer.class, userId, courseId);
            for (Integer id : ls) if (p.contains(id)) done.add(id);
        }
        Integer next = null;
        for (Integer id : ls) if (!done.contains(id)) {
            next = id;
            break;
        }
        if (next == null && !ls.isEmpty()) next = ls.get(0);

        List<Integer> quizIds = jdbc.queryForList(
                "SELECT q.QuizId FROM Quizzes q JOIN Sections s ON s.SectionId = q.SectionId WHERE s.CourseId = ? ORDER BY s.Position, s.SectionId",
                Integer.class, courseId);
        Map<String, Object> quizzes = new LinkedHashMap<>();
        int passedQuizzes = 0;
        for (Integer qid : quizIds) {
            boolean passed = false;
            Object best = null;
            int attempts = 0;
            if (en) {
                Map<String, Object> a = jdbc.queryForList(
                        "SELECT COUNT(*) AS N, MAX(Score) AS Best, MAX(CASE WHEN Passed = 1 THEN 1 ELSE 0 END) AS P FROM QuizAttempts WHERE QuizId = ? AND UserId = ?",
                        qid, userId).get(0);
                attempts = Rows.i(a, "N");
                passed = Rows.i(a, "P") == 1;
                best = attempts > 0 ? Rows.in(a, "Best") : null;
            }
            Map<String, Object> q = new LinkedHashMap<>();
            q.put("passed", passed);
            q.put("best", best);
            q.put("attempts", attempts);
            quizzes.put(String.valueOf(qid), q);
            if (passed) passedQuizzes++;
        }
        int items = ls.size() + quizIds.size();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enrolled", en);
        out.put("completed", done);
        out.put("quizzes", quizzes);
        out.put("totalQuizzes", quizIds.size());
        out.put("passedQuizzes", passedQuizzes);
        out.put("percent", items == 0 ? 0 : (int) Math.round(100.0 * (done.size() + passedQuizzes) / items));
        out.put("nextLessonId", next);
        return out;
    }

    /* ------------------------------------------------------------------ course fields (create / edit) */

    private record Fields(String title, String description, int categoryId, String level, double price, String imageUrl, String details) {
    }

    /** Validates title, price, category, level and details. details == null means "keep the stored details". */
    private Fields fields(Map<String, Object> b, boolean keepDetailsWhenMissing) {
        String title = In.trim(b, "title");
        need(!title.isEmpty(), "Title is required");
        Double price = In.num(b, "price");
        need(price != null && price >= 0, "Price must be 0 or more");
        Integer cat = In.intv(b, "categoryId");
        need(cat != null && exists("SELECT 1 FROM Categories WHERE CategoryId = ?", cat), "Choose a category");
        String level = In.str(b, "level");
        need("Beginner".equals(level) || "Intermediate".equals(level) || "Advanced".equals(level), "Invalid level");
        String details;
        if (!b.containsKey("details") && keepDetailsWhenMissing) details = null;
        else details = b.get("details") instanceof Map ? Json.write(b.get("details")) : "{}";
        String image = In.trim(b, "imageUrl");
        return new Fields(In.cut(title, 150), In.str(b, "description") == null ? "" : In.str(b, "description"), cat, level, price,
                image.isEmpty() ? null : In.cut(image, 500), details);
    }

    private record Owner(String status, int instructorId, String rejectReason) {
    }

    /** Fields only an admin can set: status and owner. */
    private Owner adminFields(Map<String, Object> b) {
        String status = In.trim(b, "status").isEmpty() ? "DRAFT" : In.trim(b, "status");
        need(List.of("DRAFT", "PENDING", "PUBLISHED", "REJECTED").contains(status), "Invalid status"); // status is never null here
        Integer ins = In.intv(b, "instructorId");
        List<Map<String, Object>> u = ins == null ? new ArrayList<>() : jdbc.queryForList("SELECT Role, IsAuthor FROM Users WHERE UserId = ?", ins);
        need(!u.isEmpty() && ("ADMIN".equals(Rows.s(u.get(0), "Role")) || Rows.b(u.get(0), "IsAuthor")), "Choose an instructor");
        String reason = In.trim(b, "rejectReason");
        need(!"REJECTED".equals(status) || !reason.isEmpty(), "A reject reason is required");
        return new Owner(status, ins, "REJECTED".equals(status) ? In.cut(reason, 500) : null);
    }

    /** POST /admin/courses */
    @Transactional
    public Map<String, Object> adminCreate(Map<String, Object> b) {
        Fields f = fields(b, false);
        Owner o = adminFields(b);
        int id = insert("INSERT INTO Courses (Title, Description, CategoryId, CourseLevel, Price, ImageUrl, Status, RejectReason, InstructorId, Details) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                f.title(), f.description(), f.categoryId(), f.level(), f.price(), f.imageUrl(), o.status(), o.rejectReason(), o.instructorId(), f.details());
        return viewOrFail(id);
    }

    /** PUT /admin/courses/{id}: edits any field; omitted details are kept. */
    @Transactional
    public Map<String, Object> adminUpdate(int courseId, Map<String, Object> b) {
        row(courseId);
        Fields f = fields(b, true);
        Owner o = adminFields(b);
        update(courseId, f);
        jdbc.update("UPDATE Courses SET Status = ?, RejectReason = ?, InstructorId = ? WHERE CourseId = ?", o.status(), o.rejectReason(), o.instructorId(), courseId);
        return viewOrFail(courseId);
    }

    /** PUT /courses/{id}: title, category, level, price, image, description, details (status stays). */
    @Transactional
    public Map<String, Object> update(int courseId, Map<String, Object> b) {
        row(courseId);
        update(courseId, fields(b, false));
        return viewOrFail(courseId);
    }

    private void update(int courseId, Fields f) {
        if (f.details() == null) {
            jdbc.update("UPDATE Courses SET Title = ?, Description = ?, CategoryId = ?, CourseLevel = ?, Price = ?, ImageUrl = ? WHERE CourseId = ?",
                    f.title(), f.description(), f.categoryId(), f.level(), f.price(), f.imageUrl(), courseId);
        } else {
            jdbc.update("UPDATE Courses SET Title = ?, Description = ?, CategoryId = ?, CourseLevel = ?, Price = ?, ImageUrl = ?, Details = ? WHERE CourseId = ?",
                    f.title(), f.description(), f.categoryId(), f.level(), f.price(), f.imageUrl(), f.details(), courseId);
        }
    }

    /** DELETE /courses/{id}: refused while students are enrolled. */
    @Transactional
    public void delete(int courseId) {
        row(courseId);
        if (exists("SELECT 1 FROM Enrollments WHERE CourseId = ?", courseId)) throw new ApiException(409, "Cannot delete a course that has enrolled students");
        jdbc.update("DELETE FROM Courses WHERE CourseId = ?", courseId);
    }

    /** DELETE /admin/courses/{id}: also removes enrollments, progress, reviews and quiz attempts (foreign keys cascade). */
    @Transactional
    public void adminDelete(int courseId) {
        row(courseId);
        jdbc.update("DELETE FROM Courses WHERE CourseId = ?", courseId);
    }

    /** POST /courses/{id}/submit */
    @Transactional
    public Map<String, Object> submit(AuthUser me, int courseId) {
        Map<String, Object> c = row(courseId);
        if (Rows.i(c, "InstructorId") != me.userId()) throw new ApiException(403, "Only the course owner can submit it");
        String st = Rows.s(c, "Status");
        need("DRAFT".equals(st) || "REJECTED".equals(st), "Only draft or rejected courses can be submitted");
        need(exists("SELECT 1 FROM Lessons l JOIN Sections s ON s.SectionId = l.SectionId WHERE s.CourseId = ?", courseId),
                "Add at least one section and one lesson before submitting");
        jdbc.update("UPDATE Courses SET Status = 'PENDING', RejectReason = NULL WHERE CourseId = ?", courseId);
        act.notifyAdmins("\"" + Rows.s(c, "Title") + "\" is waiting for approval", "courses.html");
        return viewOrFail(courseId);
    }

    /** POST /admin/courses/{id}/approve */
    @Transactional
    public Map<String, Object> approve(int courseId) {
        Map<String, Object> c = row(courseId);
        need("PENDING".equals(Rows.s(c, "Status")), "Only pending courses can be approved");
        jdbc.update("UPDATE Courses SET Status = 'PUBLISHED', RejectReason = NULL WHERE CourseId = ?", courseId);
        act.notify(Rows.i(c, "InstructorId"), "Your course \"" + Rows.s(c, "Title") + "\" was approved and is now published.", "course.html?id=" + courseId);
        return viewOrFail(courseId);
    }

    /** POST /admin/courses/{id}/reject */
    @Transactional
    public Map<String, Object> reject(int courseId, Map<String, Object> b) {
        Map<String, Object> c = row(courseId);
        need("PENDING".equals(Rows.s(c, "Status")), "Only pending courses can be rejected");
        String reason = In.trim(b, "reason");
        need(!reason.isEmpty(), "A reason is required");
        reason = In.cut(reason, 500);
        jdbc.update("UPDATE Courses SET Status = 'REJECTED', RejectReason = ? WHERE CourseId = ?", reason, courseId);
        act.notify(Rows.i(c, "InstructorId"), "Your course \"" + Rows.s(c, "Title") + "\" was rejected: " + reason, "course.html?id=" + courseId);
        return viewOrFail(courseId);
    }

    /** GET /courses/{id}/students */
    public List<Map<String, Object>> students(int courseId) {
        row(courseId);
        int total = jdbc.queryForObject("SELECT COUNT(*) FROM Lessons l JOIN Sections s ON s.SectionId = l.SectionId WHERE s.CourseId = ?", Integer.class, courseId);
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT u.UserId, u.FullName, u.Email, e.EnrolledAt, "
                        + "(SELECT COUNT(*) FROM LessonProgress p JOIN Lessons l ON l.LessonId = p.LessonId JOIN Sections s ON s.SectionId = l.SectionId "
                        + " WHERE p.UserId = e.UserId AND s.CourseId = e.CourseId) AS Done "
                        + "FROM Enrollments e JOIN Users u ON u.UserId = e.UserId WHERE e.CourseId = ? ORDER BY e.EnrollmentId", courseId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            int done = Rows.i(r, "Done");
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("userId", Rows.i(r, "UserId"));
            o.put("fullName", Rows.s(r, "FullName"));
            o.put("email", Rows.s(r, "Email"));
            o.put("enrolledAt", Rows.ts(r, "EnrolledAt"));
            o.put("completedLessons", done);
            o.put("totalLessons", total);
            o.put("percent", total == 0 ? 0 : (int) Math.round(100.0 * done / total));
            out.add(o);
        }
        return out;
    }

    /* ------------------------------------------------------------------ sections */

    private Map<String, Object> sectionRow(int sectionId) {
        List<Map<String, Object>> l = jdbc.queryForList("SELECT SectionId, CourseId, Title, Position FROM Sections WHERE SectionId = ?", sectionId);
        if (l.isEmpty()) throw new ApiException(404, "Section not found");
        return l.get(0);
    }

    private Map<String, Object> sectionJson(int sectionId, String title, int position) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("sectionId", sectionId);
        s.put("title", title);
        s.put("position", position);
        s.put("lessons", new ArrayList<>());
        s.put("quiz", null);
        return s;
    }

    @Transactional
    public Map<String, Object> addSection(int courseId, Map<String, Object> b) {
        row(courseId);
        String title = In.trim(b, "title");
        need(!title.isEmpty(), "Section title is required");
        int pos = jdbc.queryForObject("SELECT COUNT(*) FROM Sections WHERE CourseId = ?", Integer.class, courseId) + 1;
        int id = insert("INSERT INTO Sections (CourseId, Title, Position) VALUES (?, ?, ?)", courseId, In.cut(title, 150), pos);
        return sectionJson(id, In.cut(title, 150), pos);
    }

    @Transactional
    public Map<String, Object> renameSection(int sectionId, Map<String, Object> b) {
        Map<String, Object> s = sectionRow(sectionId);
        String title = In.trim(b, "title");
        need(!title.isEmpty(), "Section title is required");
        jdbc.update("UPDATE Sections SET Title = ? WHERE SectionId = ?", In.cut(title, 150), sectionId);
        return sectionJson(sectionId, In.cut(title, 150), Rows.i(s, "Position"));
    }

    @Transactional
    public void deleteSection(int sectionId) {
        Map<String, Object> s = sectionRow(sectionId);
        jdbc.update("DELETE FROM Sections WHERE SectionId = ?", sectionId);
        renumberSections(Rows.i(s, "CourseId"));
    }

    @Transactional
    public void moveSection(int sectionId, Map<String, Object> b) {
        Map<String, Object> s = sectionRow(sectionId);
        int courseId = Rows.i(s, "CourseId");
        List<Integer> order = jdbc.queryForList("SELECT SectionId FROM Sections WHERE CourseId = ? ORDER BY Position, SectionId", Integer.class, courseId);
        if (swap(order, sectionId, In.num(b, "direction"))) {
            for (int k = 0; k < order.size(); k++) jdbc.update("UPDATE Sections SET Position = ? WHERE SectionId = ?", k + 1, order.get(k));
        }
    }

    private void renumberSections(int courseId) {
        List<Integer> order = jdbc.queryForList("SELECT SectionId FROM Sections WHERE CourseId = ? ORDER BY Position, SectionId", Integer.class, courseId);
        for (int k = 0; k < order.size(); k++) jdbc.update("UPDATE Sections SET Position = ? WHERE SectionId = ?", k + 1, order.get(k));
    }

    /** Swaps the item with its neighbour (direction < 0 up, otherwise down). False when it is already at the edge. */
    private static boolean swap(List<Integer> order, int id, Double direction) {
        int i = order.indexOf(id);
        if (i < 0) return false;
        int j = i + (direction != null && direction < 0 ? -1 : 1);
        if (j < 0 || j >= order.size()) return false;
        Integer t = order.get(i);
        order.set(i, order.get(j));
        order.set(j, t);
        return true;
    }

    /* ------------------------------------------------------------------ lessons */

    private record LessonFields(String title, String type, String contentUrl, String text, int minutes, boolean preview) {
    }

    private LessonFields lessonFields(Map<String, Object> b) {
        String title = In.trim(b, "title");
        need(!title.isEmpty(), "Lesson title is required");
        String type = In.str(b, "type");
        need("VIDEO".equals(type) || "TEXT".equals(type) || "FILE".equals(type), "Invalid lesson type");
        String url = In.trim(b, "contentUrl");
        need("TEXT".equals(type) || url.toLowerCase().matches("^https?://.*"), "Enter a valid http(s) link for video or file lessons");
        Double minutes = In.num(b, "minutes");
        need(minutes == null || minutes >= 0, "Minutes must be 0 or more");
        String text = In.str(b, "text");
        return new LessonFields(In.cut(title, 150), type, url.isEmpty() ? null : In.cut(url, 500), text == null ? "" : text,
                minutes == null ? 0 : (int) Math.round(minutes), In.bool(b, "preview"));
    }

    private Map<String, Object> lessonRow(int lessonId) {
        List<Map<String, Object>> l = jdbc.queryForList("SELECT LessonId, SectionId, Position FROM Lessons WHERE LessonId = ?", lessonId);
        if (l.isEmpty()) throw new ApiException(404, "Lesson not found");
        return l.get(0);
    }

    private Map<String, Object> lessonOut(int id, int position, LessonFields f) {
        Map<String, Object> lj = new LinkedHashMap<>();
        lj.put("lessonId", id);
        lj.put("position", position);
        lj.put("title", f.title());
        lj.put("type", f.type());
        lj.put("contentUrl", f.contentUrl());
        lj.put("text", f.text());
        lj.put("minutes", f.minutes());
        lj.put("preview", f.preview());
        return lj;
    }

    @Transactional
    public Map<String, Object> addLesson(int sectionId, Map<String, Object> b) {
        sectionRow(sectionId);
        LessonFields f = lessonFields(b);
        int pos = jdbc.queryForObject("SELECT COUNT(*) FROM Lessons WHERE SectionId = ?", Integer.class, sectionId) + 1;
        int id = insert("INSERT INTO Lessons (SectionId, Title, ContentType, ContentUrl, TextContent, Minutes, IsPreview, Position) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                sectionId, f.title(), f.type(), f.contentUrl(), f.text(), f.minutes(), f.preview(), pos);
        return lessonOut(id, pos, f);
    }

    @Transactional
    public Map<String, Object> updateLesson(int lessonId, Map<String, Object> b) {
        Map<String, Object> l = lessonRow(lessonId);
        LessonFields f = lessonFields(b);
        jdbc.update("UPDATE Lessons SET Title = ?, ContentType = ?, ContentUrl = ?, TextContent = ?, Minutes = ?, IsPreview = ? WHERE LessonId = ?",
                f.title(), f.type(), f.contentUrl(), f.text(), f.minutes(), f.preview(), lessonId);
        return lessonOut(lessonId, Rows.i(l, "Position"), f);
    }

    @Transactional
    public void deleteLesson(int lessonId) {
        Map<String, Object> l = lessonRow(lessonId);
        int sectionId = Rows.i(l, "SectionId");
        jdbc.update("DELETE FROM Lessons WHERE LessonId = ?", lessonId); // learner progress rows are removed by the foreign key
        List<Integer> order = jdbc.queryForList("SELECT LessonId FROM Lessons WHERE SectionId = ? ORDER BY Position, LessonId", Integer.class, sectionId);
        for (int k = 0; k < order.size(); k++) jdbc.update("UPDATE Lessons SET Position = ? WHERE LessonId = ?", k + 1, order.get(k));
    }

    @Transactional
    public void moveLesson(int lessonId, Map<String, Object> b) {
        Map<String, Object> l = lessonRow(lessonId);
        List<Integer> order = jdbc.queryForList("SELECT LessonId FROM Lessons WHERE SectionId = ? ORDER BY Position, LessonId", Integer.class, Rows.i(l, "SectionId"));
        if (swap(order, lessonId, In.num(b, "direction"))) {
            for (int k = 0; k < order.size(); k++) jdbc.update("UPDATE Lessons SET Position = ? WHERE LessonId = ?", k + 1, order.get(k));
        }
    }

    /** Course id of a section / lesson (to check ownership). */
    public int courseOfSection(int sectionId) {
        return Rows.i(sectionRow(sectionId), "CourseId");
    }

    public int courseOfLesson(int lessonId) {
        return courseOfSection(Rows.i(lessonRow(lessonId), "SectionId"));
    }

    /* ------------------------------------------------------------------ quiz */

    /** PUT /sections/{id}/quiz: replaces the quiz of the section (the quiz id is kept so old attempts stay). */
    @Transactional
    public void saveQuiz(int sectionId, Map<String, Object> b) {
        sectionRow(sectionId);
        String title = In.trim(b, "title");
        need(!title.isEmpty(), "Quiz title is required");
        Double pass = In.num(b, "passScore");
        need(pass != null && pass >= 1 && pass <= 100, "Pass score must be 1 to 100");
        List<?> qs = In.list(b, "questions");
        need(!qs.isEmpty(), "Add at least one question");
        List<String> texts = new ArrayList<>();
        List<List<Object[]>> opts = new ArrayList<>(); // per question: {text, correct}
        for (int i = 0; i < qs.size(); i++) {
            need(qs.get(i) instanceof Map, "Question " + (i + 1) + " needs text");
            @SuppressWarnings("unchecked") Map<String, Object> q = (Map<String, Object>) qs.get(i);
            String text = In.trim(q, "text");
            need(!text.isEmpty(), "Question " + (i + 1) + " needs text");
            List<Object[]> os = new ArrayList<>();
            int correct = 0;
            for (Object o : In.list(q, "options")) {
                if (!(o instanceof Map)) continue;
                @SuppressWarnings("unchecked") Map<String, Object> om = (Map<String, Object>) o;
                String ot = In.trim(om, "text");
                if (ot.isEmpty()) continue;
                boolean ok = In.bool(om, "correct");
                if (ok) correct++;
                os.add(new Object[]{In.cut(ot, 300), ok});
            }
            need(os.size() >= 2, "Question " + (i + 1) + " needs at least 2 options");
            need(correct == 1, "Question " + (i + 1) + " needs exactly one correct option");
            texts.add(In.cut(text, 500));
            opts.add(os);
        }
        int maxAttempts = Math.max(0, In.num(b, "maxAttempts") == null ? 0 : (int) Math.round(In.num(b, "maxAttempts")));
        List<Integer> existing = jdbc.queryForList("SELECT QuizId FROM Quizzes WHERE SectionId = ?", Integer.class, sectionId);
        int quizId;
        if (existing.isEmpty()) {
            quizId = insert("INSERT INTO Quizzes (SectionId, Title, PassScore, MaxAttempts) VALUES (?, ?, ?, ?)", sectionId, In.cut(title, 150), (int) Math.round(pass), maxAttempts);
        } else {
            quizId = existing.get(0);
            jdbc.update("UPDATE Quizzes SET Title = ?, PassScore = ?, MaxAttempts = ? WHERE QuizId = ?", In.cut(title, 150), (int) Math.round(pass), maxAttempts, quizId);
            jdbc.update("DELETE FROM Questions WHERE QuizId = ?", quizId); // options go with them
        }
        for (int i = 0; i < texts.size(); i++) {
            int qid = insert("INSERT INTO Questions (QuizId, QuestionText, Position) VALUES (?, ?, ?)", quizId, texts.get(i), i + 1);
            for (Object[] o : opts.get(i)) jdbc.update("INSERT INTO Options (QuestionId, OptionText, IsCorrect) VALUES (?, ?, ?)", qid, o[0], o[1]);
        }
    }

    @Transactional
    public void deleteQuiz(int sectionId) {
        sectionRow(sectionId);
        jdbc.update("DELETE FROM Quizzes WHERE SectionId = ?", sectionId);
    }
}
