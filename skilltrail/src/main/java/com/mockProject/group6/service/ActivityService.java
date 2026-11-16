package com.mockProject.group6.service;

import com.mockProject.group6.support.In;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/** Writes the activity log (admin console) and notifications (bell icon). */
@Service
public class ActivityService {

    private final JdbcTemplate jdbc;

    public ActivityService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** type = ACCOUNT / PAYMENT / ENROLLMENT / COMPLETION. Everything except type and action may be null. */
    public void log(String type, String action, Integer userId, String email, Integer courseId, String courseTitle,
                    Double amount, String method, String detail, String actor) {
        jdbc.update("INSERT INTO ActivityLogs (LogType, Action, UserId, Email, CourseId, CourseTitle, Amount, PaymentMethod, Detail, ActorName) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                type, action, userId, In.cut(email, 150), courseId, In.cut(courseTitle, 150), amount, In.cut(method, 30),
                In.cut(detail, 300), In.cut(actor, 100));
    }

    public void account(String action, Integer userId, String email, String detail, String actor) {
        log("ACCOUNT", action, userId, email, null, null, null, null, detail, actor);
    }

    public void notify(int userId, String message, String link) {
        jdbc.update("INSERT INTO Notifications (UserId, Message, Link) VALUES (?, ?, ?)", userId, In.cut(message, 500), In.cut(link, 200));
    }

    public void notifyAdmins(String message, String link) {
        List<Integer> admins = jdbc.queryForList("SELECT UserId FROM Users WHERE Role = 'ADMIN'", Integer.class);
        for (Integer id : admins) notify(id, message, link);
    }
}
