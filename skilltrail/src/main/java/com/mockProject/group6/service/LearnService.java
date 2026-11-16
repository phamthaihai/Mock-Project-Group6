package com.mockProject.group6.service;

import com.mockProject.group6.exception.ApiException;
import com.mockProject.group6.security.AuthUser;
import com.mockProject.group6.support.In;
import com.mockProject.group6.support.Rows;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Everything a student does: enroll, learn, take quizzes, review, shop (cart) and ask for support. */
@Service
public class LearnService {

    private static final List<String> PAY = List.of("CARD", "EWALLET", "BANK", "DEMO");

    private final JdbcTemplate jdbc;
    private final ActivityService act;
    private final CourseService courses;

    public LearnService(JdbcTemplate jdbc, ActivityService act, CourseService courses) {
        this.jdbc = jdbc;
        this.act = act;
        this.courses = courses;
    }

    private static void need(boolean ok, String message) {
        if (!ok) throw new ApiException(400, message);
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

    private boolean exists(String sql, Object... args) {
        return !jdbc.queryForList(sql, args).isEmpty();
    }

    /* ------------------------------------------------------------------ enrollments */

    /** GET /enrollments/me */
    public List<Map<String, Object>> myEnrollments(AuthUser me) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT EnrollmentId, CourseId, EnrolledAt FROM Enrollments WHERE UserId = ? ORDER BY EnrollmentId", me.userId());
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> e : rows) {
            int courseId = Rows.i(e, "CourseId");
            Map<String, Object> v = courses.view(courseId);
            if (v == null) continue;
            Map<String, Object> p = courses.progress(courseId, me.userId());
            @SuppressWarnings("unchecked") Map<String, Object> inst = (Map<String, Object>) v.get("instructor");
            @SuppressWarnings("unchecked") List<Object> completed = (List<Object>) p.get("completed");
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("enrollmentId", Rows.i(e, "EnrollmentId"));
            o.put("courseId", courseId);
            o.put("title", v.get("title"));
            o.put("imageUrl", v.get("imageUrl"));
            o.put("category", v.get("category"));
            o.put("instructor", inst.get("fullName"));
            o.put("studentName", me.fullName());
            o.put("totalLessons", v.get("lessonCount"));
            o.put("completedLessons", completed.size());
            o.put("totalQuizzes", p.get("totalQuizzes"));
            o.put("passedQuizzes", p.get("passedQuizzes"));
            o.put("percent", p.get("percent"));
            o.put("nextLessonId", p.get("nextLessonId"));
            o.put("enrolledAt", Rows.ts(e, "EnrolledAt"));
            out.add(o);
        }
        return out;
    }

    /** Creates the enrollment plus its logs and the notification for the instructor. Returns the new enrollment id. */
    private int doEnroll(AuthUser me, Map<String, Object> c, String method, String payDetail) {
        int courseId = Rows.i(c, "CourseId");
        double price = Rows.d(c, "Price");
        double paid = me.admin() ? 0 : price;
        String pm = me.admin() ? "ADMIN" : (price > 0 ? method : null);
        int id;
        try {
            id = insert("INSERT INTO Enrollments (CourseId, UserId, PricePaid, PaymentMethod) VALUES (?, ?, ?, ?)", courseId, me.userId(), paid, pm);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(409, "You are already enrolled in this course");
        }
        String title = Rows.s(c, "Title");
        act.log("ENROLLMENT", "ENROLLED", me.userId(), me.email(), courseId, title, null, null, null, me.fullName());
        if (paid > 0) act.log("PAYMENT", "PAID", me.userId(), me.email(), courseId, title, paid, pm, payDetail, me.fullName());
        act.notify(Rows.i(c, "InstructorId"), me.fullName() + " enrolled in \"" + title + "\"", "course.html?id=" + courseId);
        return id;
    }

    /** POST /courses/{id}/enroll */
    @Transactional
    public Map<String, Object> enroll(AuthUser me, int courseId, Map<String, Object> b) {
        Map<String, Object> c = courses.row(courseId);
        if (!"PUBLISHED".equals(Rows.s(c, "Status"))) throw new ApiException(404, "Course not found");
        if (Rows.i(c, "InstructorId") == me.userId()) throw new ApiException(400, "You cannot enroll in your own course");
        if (courses.enrolled(courseId, me.userId())) throw new ApiException(409, "You are already enrolled in this course");
        String method = In.str(b, "paymentMethod");
        if (!me.admin() && Rows.d(c, "Price") > 0 && (method == null || !PAY.contains(method))) throw new ApiException(400, "Choose a payment method");
        int id = doEnroll(me, c, method, null);
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("enrollmentId", id);
        o.put("courseId", courseId);
        o.put("title", Rows.s(c, "Title"));
        return o;
    }

    /** DELETE /courses/{id}/enroll */
    @Transactional
    public void unenroll(AuthUser me, int courseId) {
        if (!courses.enrolled(courseId, me.userId())) throw new ApiException(404, "You are not enrolled in this course");
        List<Map<String, Object>> c = jdbc.queryForList("SELECT Title FROM Courses WHERE CourseId = ?", courseId);
        act.log("ENROLLMENT", "UNENROLLED", me.userId(), me.email(), courseId, c.isEmpty() ? null : Rows.s(c.get(0), "Title"), null, null, null, me.fullName());
        jdbc.update("DELETE FROM LessonProgress WHERE UserId = ? AND LessonId IN "
                + "(SELECT l.LessonId FROM Lessons l JOIN Sections s ON s.SectionId = l.SectionId WHERE s.CourseId = ?)", me.userId(), courseId);
        jdbc.update("DELETE FROM Enrollments WHERE CourseId = ? AND UserId = ?", courseId, me.userId());
    }

    private void checkCompletion(AuthUser me, Map<String, Object> course, int was, Map<String, Object> p) {
        int after = (Integer) p.get("percent");
        if (after != 100 || was >= 100) return;
        @SuppressWarnings("unchecked") List<Object> done = (List<Object>) p.get("completed");
        int courseId = Rows.i(course, "CourseId");
        String title = Rows.s(course, "Title");
        act.log("COMPLETION", "COMPLETED", me.userId(), me.email(), courseId, title, null, null,
                done.size() + " lessons, " + p.get("passedQuizzes") + "/" + p.get("totalQuizzes") + " quizzes", me.fullName());
        act.notify(me.userId(), "Congratulations! You completed \"" + title + "\".", "congrats.html?id=" + courseId);
    }

    /** PUT /courses/{id}/progress {lessonId, done} */
    @Transactional
    public Map<String, Object> setProgress(AuthUser me, int courseId, Map<String, Object> b) {
        Map<String, Object> c = courses.row(courseId);
        if (!courses.enrolled(courseId, me.userId())) throw new ApiException(403, "Enroll in this course first");
        Integer lessonId = In.intv(b, "lessonId");
        if (lessonId == null || !exists("SELECT 1 FROM Lessons l JOIN Sections s ON s.SectionId = l.SectionId WHERE l.LessonId = ? AND s.CourseId = ?", lessonId, courseId)) {
            throw new ApiException(404, "Lesson not found");
        }
        int was = (Integer) courses.progress(courseId, me.userId()).get("percent");
        jdbc.update("DELETE FROM LessonProgress WHERE UserId = ? AND LessonId = ?", me.userId(), lessonId);
        Object d = b.get("done");
        if (!(d instanceof Boolean) || (Boolean) d) jdbc.update("INSERT INTO LessonProgress (UserId, LessonId) VALUES (?, ?)", me.userId(), lessonId);
        Map<String, Object> p = courses.progress(courseId, me.userId());
        checkCompletion(me, c, was, p);
        return p;
    }

    /* ------------------------------------------------------------------ quizzes */

    private Map<String, Object> quizRow(int quizId) {
        List<Map<String, Object>> l = jdbc.queryForList(
                "SELECT q.QuizId, q.Title, q.PassScore, q.MaxAttempts, s.CourseId, c.InstructorId FROM Quizzes q "
                        + "JOIN Sections s ON s.SectionId = q.SectionId JOIN Courses c ON c.CourseId = s.CourseId WHERE q.QuizId = ?", quizId);
        if (l.isEmpty()) throw new ApiException(404, "Quiz not found");
        return l.get(0);
    }

    private void quizAccess(AuthUser me, Map<String, Object> q) {
        if (!(me.admin() || Rows.i(q, "InstructorId") == me.userId() || courses.enrolled(Rows.i(q, "CourseId"), me.userId()))) {
            throw new ApiException(403, "Enroll in this course to take the quiz");
        }
    }

    /** GET /quizzes/{id}: the questions WITHOUT the correct answers. */
    public Map<String, Object> quiz(AuthUser me, int quizId) {
        Map<String, Object> q = quizRow(quizId);
        quizAccess(me, q);
        Map<String, Object> a = jdbc.queryForList("SELECT COUNT(*) AS N, MAX(Score) AS Best, MAX(CASE WHEN Passed = 1 THEN 1 ELSE 0 END) AS P FROM QuizAttempts WHERE QuizId = ? AND UserId = ?",
                quizId, me.userId()).get(0);
        int used = Rows.i(a, "N");
        List<Map<String, Object>> qs = jdbc.queryForList("SELECT QuestionId, QuestionText FROM Questions WHERE QuizId = ? ORDER BY Position, QuestionId", quizId);
        List<Map<String, Object>> qj = new ArrayList<>();
        for (Map<String, Object> x : qs) {
            List<Map<String, Object>> os = jdbc.queryForList("SELECT OptionId, OptionText FROM Options WHERE QuestionId = ? ORDER BY OptionId", Rows.i(x, "QuestionId"));
            List<Map<String, Object>> oj = new ArrayList<>();
            for (Map<String, Object> o : os) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("optionId", Rows.i(o, "OptionId"));
                m.put("text", Rows.s(o, "OptionText"));
                oj.add(m);
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("questionId", Rows.i(x, "QuestionId"));
            m.put("text", Rows.s(x, "QuestionText"));
            m.put("options", oj);
            qj.add(m);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("quizId", quizId);
        out.put("courseId", Rows.i(q, "CourseId"));
        out.put("title", Rows.s(q, "Title"));
        out.put("passScore", Rows.i(q, "PassScore"));
        out.put("maxAttempts", Rows.i(q, "MaxAttempts"));
        out.put("attemptsUsed", used);
        out.put("bestScore", used > 0 ? Rows.in(a, "Best") : null);
        out.put("passed", Rows.i(a, "P") == 1);
        out.put("questions", qj);
        return out;
    }

    /** POST /quizzes/{id}/attempts {answers:{questionId: optionId}} */
    @Transactional
    public Map<String, Object> attempt(AuthUser me, int quizId, Map<String, Object> b) {
        Map<String, Object> q = quizRow(quizId);
        quizAccess(me, q);
        int courseId = Rows.i(q, "CourseId");
        int used = jdbc.queryForObject("SELECT COUNT(*) FROM QuizAttempts WHERE QuizId = ? AND UserId = ?", Integer.class, quizId, me.userId());
        int max = Rows.i(q, "MaxAttempts");
        if (!me.admin() && max > 0 && used >= max) throw new ApiException(409, "You have used all attempts for this quiz");
        Object raw = b.get("answers");
        Map<?, ?> ans = raw instanceof Map ? (Map<?, ?>) raw : Map.of();
        List<Map<String, Object>> qs = jdbc.queryForList("SELECT QuestionId FROM Questions WHERE QuizId = ? ORDER BY Position, QuestionId", quizId);
        for (Map<String, Object> x : qs) need(ans.get(String.valueOf(Rows.i(x, "QuestionId"))) != null, "Answer every question before submitting");
        Map<String, Object> correct = new LinkedHashMap<>();
        int ok = 0;
        for (Map<String, Object> x : qs) {
            int qid = Rows.i(x, "QuestionId");
            List<Integer> good = jdbc.queryForList("SELECT OptionId FROM Options WHERE QuestionId = ? AND IsCorrect = 1", Integer.class, qid);
            Integer right = good.isEmpty() ? null : good.get(0);
            correct.put(String.valueOf(qid), right);
            if (right != null && right.equals(In.toInt(ans.get(String.valueOf(qid))))) ok++;
        }
        int was = (Integer) courses.progress(courseId, me.userId()).get("percent");
        int score = qs.isEmpty() ? 0 : (int) Math.round(100.0 * ok / qs.size());
        boolean passed = score >= Rows.i(q, "PassScore");
        jdbc.update("INSERT INTO QuizAttempts (QuizId, UserId, Score, Passed) VALUES (?, ?, ?, ?)", quizId, me.userId(), score, passed);
        Map<String, Object> p = courses.progress(courseId, me.userId());
        checkCompletion(me, courses.row(courseId), was, p);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("score", score);
        out.put("passed", passed);
        out.put("passScore", Rows.i(q, "PassScore"));
        out.put("correctCount", ok);
        out.put("total", qs.size());
        out.put("correct", correct);
        out.put("attemptsUsed", used + 1);
        out.put("courseCompleted", (Integer) p.get("percent") == 100);
        return out;
    }

    /* ------------------------------------------------------------------ reviews */

    private static Map<String, Object> reviewJson(int id, int courseId, int userId, int rating, String comment, String createdAt) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("reviewId", id);
        r.put("courseId", courseId);
        r.put("userId", userId);
        r.put("rating", rating);
        r.put("comment", comment);
        r.put("createdAt", createdAt);
        return r;
    }

    /** GET /courses/{id}/reviews */
    public List<Map<String, Object>> reviews(int courseId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT r.ReviewId, r.UserId, u.FullName AS UserName, r.Rating, r.Comment, r.CreatedAt FROM Reviews r JOIN Users u ON u.UserId = r.UserId "
                        + "WHERE r.CourseId = ? ORDER BY r.CreatedAt DESC, r.ReviewId DESC", courseId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("reviewId", Rows.i(r, "ReviewId"));
            o.put("userId", Rows.i(r, "UserId"));
            o.put("userName", Rows.s(r, "UserName"));
            o.put("rating", Rows.i(r, "Rating"));
            o.put("comment", Rows.sn(r, "Comment"));
            o.put("createdAt", Rows.ts(r, "CreatedAt"));
            out.add(o);
        }
        return out;
    }

    private static int rating(Map<String, Object> b) {
        Double r = In.num(b, "rating");
        need(r != null && r == Math.rint(r) && r >= 1 && r <= 5, "Rating must be 1 to 5");
        return (int) Math.round(r);
    }

    /** POST /courses/{id}/reviews */
    @Transactional
    public Map<String, Object> addReview(AuthUser me, int courseId, Map<String, Object> b) {
        courses.row(courseId);
        if (!courses.enrolled(courseId, me.userId())) throw new ApiException(403, "Only enrolled students can review this course");
        if (exists("SELECT 1 FROM Reviews WHERE CourseId = ? AND UserId = ?", courseId, me.userId())) throw new ApiException(409, "You have already reviewed this course");
        int rating = rating(b);
        String comment = In.cut(In.trim(b, "comment"), 1000);
        int id;
        try {
            id = insert("INSERT INTO Reviews (CourseId, UserId, Rating, Comment) VALUES (?, ?, ?, ?)", courseId, me.userId(), rating, comment);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(409, "You have already reviewed this course");
        }
        return reviewJson(id, courseId, me.userId(), rating, comment, Rows.ts(jdbc.queryForList("SELECT CreatedAt FROM Reviews WHERE ReviewId = ?", id).get(0), "CreatedAt"));
    }

    private Map<String, Object> reviewRow(int reviewId) {
        List<Map<String, Object>> l = jdbc.queryForList("SELECT ReviewId, CourseId, UserId, Rating, Comment, CreatedAt FROM Reviews WHERE ReviewId = ?", reviewId);
        if (l.isEmpty()) throw new ApiException(404, "Review not found");
        return l.get(0);
    }

    /** PUT /reviews/{id} (own review only) */
    @Transactional
    public Map<String, Object> editReview(AuthUser me, int reviewId, Map<String, Object> b) {
        Map<String, Object> r = reviewRow(reviewId);
        if (Rows.i(r, "UserId") != me.userId()) throw new ApiException(403, "You can only edit your own review");
        int rating = rating(b);
        String comment = In.cut(In.trim(b, "comment"), 1000);
        jdbc.update("UPDATE Reviews SET Rating = ?, Comment = ? WHERE ReviewId = ?", rating, comment, reviewId);
        return reviewJson(reviewId, Rows.i(r, "CourseId"), me.userId(), rating, comment, Rows.ts(r, "CreatedAt"));
    }

    /** DELETE /reviews/{id} (own review, or any review for an admin) */
    @Transactional
    public void deleteReview(AuthUser me, int reviewId) {
        Map<String, Object> r = reviewRow(reviewId);
        if (Rows.i(r, "UserId") != me.userId() && !me.admin()) throw new ApiException(403, "You can only delete your own review");
        jdbc.update("DELETE FROM Reviews WHERE ReviewId = ?", reviewId);
    }

    /* ------------------------------------------------------------------ cart */

    private List<Map<String, Object>> cartRows(AuthUser me) {
        List<Map<String, Object>> items = jdbc.queryForList("SELECT CourseId, AddedAt FROM CartItems WHERE UserId = ? ORDER BY AddedAt, CourseId", me.userId());
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> i : items) {
            Map<String, Object> v = courses.view(Rows.i(i, "CourseId"));
            if (v == null) continue;
            v.put("addedAt", Rows.ts(i, "AddedAt"));
            out.add(v);
        }
        return out;
    }

    private static double total(List<Map<String, Object>> rows) {
        double t = 0;
        for (Map<String, Object> r : rows) t += ((Number) r.get("price")).doubleValue();
        return t;
    }

    private static void notAdmin(AuthUser me) {
        if (me.admin()) throw new ApiException(400, "Admins enroll without payment, so they do not use the cart");
    }

    /** GET /cart */
    public Map<String, Object> cart(AuthUser me) {
        List<Map<String, Object>> rows = cartRows(me);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", rows);
        out.put("count", rows.size());
        out.put("total", total(rows));
        return out;
    }

    /** POST /cart/{courseId} */
    @Transactional
    public Map<String, Object> addToCart(AuthUser me, int courseId) {
        notAdmin(me);
        Map<String, Object> c = courses.row(courseId);
        if (!"PUBLISHED".equals(Rows.s(c, "Status"))) throw new ApiException(404, "Course not found");
        if (Rows.i(c, "InstructorId") == me.userId()) throw new ApiException(400, "You cannot buy your own course");
        if (courses.enrolled(courseId, me.userId())) throw new ApiException(409, "You are already enrolled in this course");
        if (exists("SELECT 1 FROM CartItems WHERE UserId = ? AND CourseId = ?", me.userId(), courseId)) throw new ApiException(409, "This course is already in your cart");
        try {
            jdbc.update("INSERT INTO CartItems (UserId, CourseId) VALUES (?, ?)", me.userId(), courseId);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(409, "This course is already in your cart");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("courseId", courseId);
        out.put("count", jdbc.queryForObject("SELECT COUNT(*) FROM CartItems WHERE UserId = ?", Integer.class, me.userId()));
        return out;
    }

    /** DELETE /cart/{courseId} */
    @Transactional
    public void removeFromCart(AuthUser me, int courseId) {
        if (jdbc.update("DELETE FROM CartItems WHERE UserId = ? AND CourseId = ?", me.userId(), courseId) == 0) {
            throw new ApiException(404, "That course is not in your cart");
        }
    }

    /** DELETE /cart */
    public void clearCart(AuthUser me) {
        jdbc.update("DELETE FROM CartItems WHERE UserId = ?", me.userId());
    }

    /** POST /cart/checkout {paymentMethod}: enrolls in every course of the cart (same records as POST /courses/{id}/enroll). */
    @Transactional
    public Map<String, Object> checkout(AuthUser me, Map<String, Object> b) {
        notAdmin(me);
        List<Map<String, Object>> rows = cartRows(me);
        need(!rows.isEmpty(), "Your cart is empty");
        String method = In.str(b, "paymentMethod");
        boolean anyPaid = false;
        for (Map<String, Object> r : rows) if (((Number) r.get("price")).doubleValue() > 0) anyPaid = true;
        if (anyPaid && (method == null || !PAY.contains(method))) throw new ApiException(400, "Choose a payment method");
        List<Map<String, Object>> done = new ArrayList<>(), skipped = new ArrayList<>();
        int last = 0;
        double sum = 0;
        for (Map<String, Object> v : rows) {
            int courseId = (Integer) v.get("courseId");
            Map<String, Object> c = courses.row(courseId);
            if (!"PUBLISHED".equals(Rows.s(c, "Status")) || Rows.i(c, "InstructorId") == me.userId() || courses.enrolled(courseId, me.userId())) {
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("courseId", courseId);
                s.put("title", Rows.s(c, "Title"));
                skipped.add(s);
                continue;
            }
            int id = doEnroll(me, c, method, "Cart checkout");
            last = id;
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("enrollmentId", id);
            d.put("courseId", courseId);
            d.put("title", Rows.s(c, "Title"));
            d.put("price", Rows.d(c, "Price"));
            done.add(d);
            sum += Rows.d(c, "Price");
        }
        jdbc.update("DELETE FROM CartItems WHERE UserId = ?", me.userId());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("orderNumber", done.isEmpty() ? null : "LH-" + String.format("%06d", last));
        out.put("paymentMethod", method);
        out.put("items", done);
        out.put("skipped", skipped);
        out.put("total", sum);
        return out;
    }

    /* ------------------------------------------------------------------ support tickets */

    static Map<String, Object> ticketJson(Map<String, Object> t) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("ticketId", Rows.i(t, "TicketId"));
        o.put("userId", Rows.in(t, "UserId"));
        o.put("name", Rows.s(t, "Name"));
        o.put("email", Rows.s(t, "Email"));
        o.put("category", Rows.s(t, "Category"));
        o.put("courseId", Rows.in(t, "CourseId"));
        o.put("courseTitle", Rows.sn(t, "CourseTitle"));
        o.put("subject", Rows.s(t, "Subject"));
        o.put("message", Rows.s(t, "Message"));
        o.put("status", Rows.s(t, "Status"));
        o.put("reply", Rows.s(t, "Reply"));
        o.put("repliedAt", Rows.ts(t, "RepliedAt"));
        o.put("createdAt", Rows.ts(t, "CreatedAt"));
        return o;
    }

    static final String TICKET_SELECT = "SELECT t.TicketId, t.UserId, t.Name, t.Email, t.Category, t.CourseId, c.Title AS CourseTitle, t.Subject, t.Message, t.Status, t.Reply, t.RepliedAt, t.CreatedAt "
            + "FROM SupportTickets t LEFT JOIN Courses c ON c.CourseId = t.CourseId ";

    /** POST /support (guests may send one) */
    @Transactional
    public Map<String, Object> createTicket(AuthUser me, Map<String, Object> b) {
        String name = me != null ? me.fullName() : In.trim(b, "name");
        String email = me != null ? me.email() : In.trim(b, "email");
        need(!name.isEmpty(), "Name is required");
        need(email.matches("^\\S+@\\S+\\.\\S+$"), "Enter a valid email");
        String category = In.str(b, "category");
        need(category != null && List.of("PAYMENT", "COURSE", "ACCOUNT", "OTHER").contains(category), "Choose a topic");
        String subject = In.trim(b, "subject");
        need(subject.length() >= 3, "Subject is required");
        String message = In.trim(b, "message");
        need(message.length() >= 10, "Message must be at least 10 characters");
        Integer courseId = In.intv(b, "courseId");
        if (courseId != null && !exists("SELECT 1 FROM Courses WHERE CourseId = ?", courseId)) courseId = null;
        subject = In.cut(subject, 100);
        int id = insert("INSERT INTO SupportTickets (UserId, Name, Email, Category, CourseId, Subject, Message) VALUES (?, ?, ?, ?, ?, ?, ?)",
                me == null ? null : me.userId(), In.cut(name, 100), In.cut(email, 150), category, courseId, subject, In.cut(message, 1000));
        act.notifyAdmins("New support ticket: \"" + subject + "\"", "admin.html");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ticketId", id);
        return out;
    }

    /** GET /support/me */
    public List<Map<String, Object>> myTickets(AuthUser me) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> t : jdbc.queryForList(TICKET_SELECT + "WHERE t.UserId = ? ORDER BY t.CreatedAt DESC, t.TicketId DESC", me.userId())) out.add(ticketJson(t));
        return out;
    }
}
