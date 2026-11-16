package com.mockProject.group6.service;

import com.mockProject.group6.exception.ApiException;
import com.mockProject.group6.security.AuthUser;
import com.mockProject.group6.support.Db;
import com.mockProject.group6.support.In;
import com.mockProject.group6.support.Rows;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Admin console: users, categories, support inbox, logs, reviews and dashboard numbers. */
@Service
public class AdminService {

    private final JdbcTemplate jdbc;
    private final ActivityService act;

    public AdminService(JdbcTemplate jdbc, ActivityService act) {
        this.jdbc = jdbc;
        this.act = act;
    }

    private static void need(boolean ok, String message) {
        if (!ok) throw new ApiException(400, message);
    }

    /* ------------------------------------------------------------------ categories */

    private static Map<String, Object> category(int id, String name) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("categoryId", id);
        o.put("name", name);
        return o;
    }

    /** GET /categories */
    public List<Map<String, Object>> categories() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> k : jdbc.queryForList("SELECT CategoryId, Name FROM Categories ORDER BY CategoryId")) out.add(category(Rows.i(k, "CategoryId"), Rows.s(k, "Name")));
        return out;
    }

    public Map<String, Object> addCategory(Map<String, Object> b) {
        String name = In.cut(In.trim(b, "name"), 50);
        need(!name.isEmpty(), "Category name is required");
        if (!jdbc.queryForList("SELECT 1 FROM Categories WHERE LOWER(Name) = LOWER(?)", name).isEmpty()) throw new ApiException(409, "Category already exists");
        try {
            return category(Db.insert(jdbc, "INSERT INTO Categories (Name) VALUES (?)", name), name);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(409, "Category already exists");
        }
    }

    public Map<String, Object> renameCategory(int id, Map<String, Object> b) {
        if (jdbc.queryForList("SELECT 1 FROM Categories WHERE CategoryId = ?", id).isEmpty()) throw new ApiException(404, "Category not found");
        String name = In.cut(In.trim(b, "name"), 50);
        need(!name.isEmpty(), "Category name is required");
        if (!jdbc.queryForList("SELECT 1 FROM Categories WHERE LOWER(Name) = LOWER(?) AND CategoryId <> ?", name, id).isEmpty()) throw new ApiException(409, "Category already exists");
        jdbc.update("UPDATE Categories SET Name = ? WHERE CategoryId = ?", name, id);
        return category(id, name);
    }

    public void deleteCategory(int id) {
        if (jdbc.queryForList("SELECT 1 FROM Categories WHERE CategoryId = ?", id).isEmpty()) throw new ApiException(404, "Category not found");
        if (!jdbc.queryForList("SELECT 1 FROM Courses WHERE CategoryId = ?", id).isEmpty()) throw new ApiException(409, "Cannot delete a category that has courses");
        jdbc.update("DELETE FROM Categories WHERE CategoryId = ?", id);
    }

    /* ------------------------------------------------------------------ users */

    /** GET /admin/users */
    public List<Map<String, Object>> users() {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT u.UserId, u.FullName, u.Email, u.PasswordHash, u.Role, u.IsAuthor, u.Locked, u.Verified, u.GoogleLinked, u.CreatedAt, "
                        + "(SELECT COUNT(*) FROM Enrollments e WHERE e.UserId = u.UserId) AS Enr, "
                        + "(SELECT COUNT(*) FROM Courses c WHERE c.InstructorId = u.UserId) AS Crs FROM Users u ORDER BY u.UserId");
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> u : rows) {
            Map<String, Object> o = AccountService.pubUser(u);
            o.put("verified", Rows.b(u, "Verified"));
            o.put("createdAt", Rows.ts(u, "CreatedAt"));
            o.put("enrollments", Rows.i(u, "Enr"));
            o.put("courses", Rows.i(u, "Crs"));
            out.add(o);
        }
        return out;
    }

    private Map<String, Object> userRow(int id) {
        List<Map<String, Object>> l = jdbc.queryForList("SELECT UserId, FullName, Email, Role FROM Users WHERE UserId = ?", id);
        if (l.isEmpty()) throw new ApiException(404, "User not found");
        return l.get(0);
    }

    /** PUT /admin/users/{id}/role {role} */
    public void changeRole(AuthUser admin, int id, Map<String, Object> b) {
        Map<String, Object> u = userRow(id);
        if (id == admin.userId()) throw new ApiException(400, "You cannot change your own role");
        String role = In.str(b, "role");
        need("STUDENT".equals(role) || "ADMIN".equals(role), "Invalid role");
        act.account("ROLE_CHANGED", id, Rows.s(u, "Email"), Rows.s(u, "Role") + " -> " + role, admin.fullName());
        jdbc.update("UPDATE Users SET Role = ? WHERE UserId = ?", role, id);
    }

    /** PUT /admin/users/{id}/lock {locked} */
    public void lock(AuthUser admin, int id, Map<String, Object> b) {
        Map<String, Object> u = userRow(id);
        if (id == admin.userId()) throw new ApiException(400, "You cannot lock your own account");
        boolean locked = In.bool(b, "locked");
        jdbc.update("UPDATE Users SET Locked = ? WHERE UserId = ?", locked, id);
        act.account(locked ? "LOCKED" : "UNLOCKED", id, Rows.s(u, "Email"), null, admin.fullName());
    }

    /* ------------------------------------------------------------------ support inbox */

    /** GET /admin/tickets */
    public List<Map<String, Object>> tickets() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> t : jdbc.queryForList(LearnService.TICKET_SELECT + "ORDER BY t.CreatedAt DESC, t.TicketId DESC")) out.add(LearnService.ticketJson(t));
        return out;
    }

    /** PUT /admin/tickets/{id} {status?, reply?} */
    public Map<String, Object> updateTicket(int id, Map<String, Object> b) {
        List<Map<String, Object>> l = jdbc.queryForList("SELECT TicketId, UserId, Subject, Status FROM SupportTickets WHERE TicketId = ?", id);
        if (l.isEmpty()) throw new ApiException(404, "Ticket not found");
        Map<String, Object> t = l.get(0);
        String status = In.trim(b, "status").isEmpty() ? Rows.s(t, "Status") : In.trim(b, "status");
        need("OPEN".equals(status) || "RESOLVED".equals(status), "Invalid status");
        String reply = In.trim(b, "reply");
        if (!reply.isEmpty()) {
            jdbc.update("UPDATE SupportTickets SET Reply = ?, RepliedAt = SYSDATETIME() WHERE TicketId = ?", In.cut(reply, 1000), id);
            Integer userId = Rows.in(t, "UserId");
            if (userId != null) act.notify(userId, "Support replied to \"" + Rows.s(t, "Subject") + "\"", "support.html#support");
        }
        jdbc.update("UPDATE SupportTickets SET Status = ? WHERE TicketId = ?", status, id);
        return LearnService.ticketJson(jdbc.queryForList(LearnService.TICKET_SELECT + "WHERE t.TicketId = ?", id).get(0));
    }

    /* ------------------------------------------------------------------ logs, reviews, stats */

    /** GET /admin/logs/{type}: latest 500 entries of ACCOUNT, PAYMENT, ENROLLMENT or COMPLETION. */
    public List<Map<String, Object>> logs(String type) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT TOP 500 l.LogId, l.LogType, l.Action, l.UserId, u.FullName AS UserName, l.Email, l.CourseId, l.CourseTitle, l.Amount, l.PaymentMethod, l.Detail, l.ActorName, l.CreatedAt "
                        + "FROM ActivityLogs l LEFT JOIN Users u ON u.UserId = l.UserId WHERE l.LogType = ? ORDER BY l.CreatedAt DESC, l.LogId DESC", type.toUpperCase());
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> l : rows) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("logId", l.get("LogId") instanceof Number ? ((Number) l.get("LogId")).longValue() : 0L);
            o.put("type", Rows.s(l, "LogType"));
            o.put("action", Rows.s(l, "Action"));
            o.put("userId", Rows.in(l, "UserId"));
            o.put("userName", Rows.sn(l, "UserName"));
            o.put("email", Rows.sn(l, "Email"));
            o.put("courseId", Rows.in(l, "CourseId"));
            o.put("courseTitle", Rows.sn(l, "CourseTitle"));
            o.put("amount", l.get("Amount") == null ? null : Rows.d(l, "Amount"));
            o.put("method", Rows.s(l, "PaymentMethod"));
            o.put("detail", Rows.sn(l, "Detail"));
            o.put("actor", Rows.sn(l, "ActorName"));
            o.put("createdAt", Rows.ts(l, "CreatedAt"));
            out.add(o);
        }
        return out;
    }

    /** GET /admin/reviews: latest 50 */
    public List<Map<String, Object>> reviews() {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT TOP 50 r.ReviewId, c.Title AS CourseTitle, u.FullName AS UserName, r.Rating, r.Comment, r.CreatedAt FROM Reviews r "
                        + "JOIN Courses c ON c.CourseId = r.CourseId JOIN Users u ON u.UserId = r.UserId ORDER BY r.ReviewId DESC");
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("reviewId", Rows.i(r, "ReviewId"));
            o.put("courseTitle", Rows.s(r, "CourseTitle"));
            o.put("userName", Rows.s(r, "UserName"));
            o.put("rating", Rows.i(r, "Rating"));
            o.put("comment", Rows.sn(r, "Comment"));
            o.put("createdAt", Rows.ts(r, "CreatedAt"));
            out.add(o);
        }
        return out;
    }

    private int count(String sql) {
        Integer n = jdbc.queryForObject(sql, Integer.class);
        return n == null ? 0 : n;
    }

    /** GET /admin/stats */
    public Map<String, Object> stats() {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("users", count("SELECT COUNT(*) FROM Users"));
        o.put("courses", count("SELECT COUNT(*) FROM Courses"));
        o.put("published", count("SELECT COUNT(*) FROM Courses WHERE Status = 'PUBLISHED'"));
        o.put("pending", count("SELECT COUNT(*) FROM Courses WHERE Status = 'PENDING'"));
        o.put("enrollments", count("SELECT COUNT(*) FROM Enrollments"));
        o.put("reviews", count("SELECT COUNT(*) FROM Reviews"));
        o.put("openTickets", count("SELECT COUNT(*) FROM SupportTickets WHERE Status = 'OPEN'"));
        List<Map<String, Object>> top = new ArrayList<>();
        for (Map<String, Object> c : jdbc.queryForList(
                "SELECT TOP 5 c.CourseId, c.Title, (SELECT COUNT(*) FROM Enrollments e WHERE e.CourseId = c.CourseId) AS Enrolled "
                        + "FROM Courses c WHERE c.Status = 'PUBLISHED' ORDER BY Enrolled DESC, c.CourseId")) {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("courseId", Rows.i(c, "CourseId"));
            t.put("title", Rows.s(c, "Title"));
            t.put("enrolled", Rows.i(c, "Enrolled"));
            top.add(t);
        }
        o.put("topCourses", top);
        return o;
    }
}
