package com.mockProject.group6.controller;

import com.mockProject.group6.entity.User;
import com.mockProject.group6.service.AdminAccess;
import com.mockProject.group6.service.AdminApiException;
import com.mockProject.group6.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminApiController {

    private final AdminAccess access;
    private final AdminService admin;

    public AdminApiController(AdminAccess access, AdminService admin) {
        this.access = access;
        this.admin = admin;
    }

    @ExceptionHandler(AdminApiException.class)
    public ResponseEntity<Map<String, String>> handleAdminError(AdminApiException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(Map.of("message", exception.getMessage()));
    }

    @GetMapping("/admin/session")
    public Map<String, Object> session(HttpSession session) {
        User user = access.requireAdmin(session);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("fullName", user.getFullName());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());
        result.put("role", "ADMIN");
        return result;
    }

    @GetMapping("/admin/courses")
    public List<Map<String, Object>> courses(HttpSession session) {
        access.requireAdmin(session);
        return admin.courses();
    }

    @PostMapping("/admin/courses")
    public Map<String, Object> createCourse(
            HttpSession session,
            @RequestBody(required = false) Map<String, Object> body) {
        return admin.createCourse(bodyOrEmpty(body), access.requireAdmin(session));
    }

    @PutMapping("/admin/courses/{id}")
    public Map<String, Object> updateCourse(
            HttpSession session, @PathVariable int id,
            @RequestBody(required = false) Map<String, Object> body) {
        return admin.updateCourse(id, bodyOrEmpty(body), access.requireAdmin(session));
    }

    @DeleteMapping("/admin/courses/{id}")
    public ResponseEntity<Void> deleteCourse(HttpSession session, @PathVariable int id) {
        access.requireAdmin(session);
        admin.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/courses/{id}/approve")
    public Map<String, Object> approveCourse(HttpSession session, @PathVariable int id) {
        access.requireAdmin(session);
        return admin.approveCourse(id);
    }

    @PostMapping("/admin/courses/{id}/reject")
    public Map<String, Object> rejectCourse(
            HttpSession session, @PathVariable int id,
            @RequestBody(required = false) Map<String, Object> body) {
        access.requireAdmin(session);
        return admin.rejectCourse(id, bodyOrEmpty(body));
    }

    @GetMapping("/categories")
    public List<Map<String, Object>> categories() {
        return admin.categories();
    }

    @PostMapping("/categories")
    public Map<String, Object> addCategory(
            HttpSession session, @RequestBody(required = false) Map<String, Object> body) {
        access.requireAdmin(session);
        return admin.addCategory(bodyOrEmpty(body));
    }

    @PutMapping("/categories/{id}")
    public Map<String, Object> renameCategory(
            HttpSession session, @PathVariable int id,
            @RequestBody(required = false) Map<String, Object> body) {
        access.requireAdmin(session);
        return admin.renameCategory(id, bodyOrEmpty(body));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(HttpSession session, @PathVariable int id) {
        access.requireAdmin(session);
        admin.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/users")
    public List<Map<String, Object>> users(HttpSession session) {
        access.requireAdmin(session);
        return admin.users();
    }

    @PutMapping("/admin/users/{id}/role")
    public ResponseEntity<Void> changeRole(
            HttpSession session, @PathVariable int id,
            @RequestBody(required = false) Map<String, Object> body) {
        admin.changeRole(access.requireAdmin(session), id, bodyOrEmpty(body));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/users/{id}/lock")
    public ResponseEntity<Void> lockUser(
            HttpSession session, @PathVariable int id,
            @RequestBody(required = false) Map<String, Object> body) {
        admin.lockUser(access.requireAdmin(session), id, bodyOrEmpty(body));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/tickets")
    public List<Map<String, Object>> tickets(HttpSession session) {
        access.requireAdmin(session);
        return admin.tickets();
    }

    @PutMapping("/admin/tickets/{id}")
    public Map<String, Object> updateTicket(
            HttpSession session, @PathVariable int id,
            @RequestBody(required = false) Map<String, Object> body) {
        access.requireAdmin(session);
        return admin.updateTicket(id, bodyOrEmpty(body));
    }

    @GetMapping("/admin/logs/{type}")
    public List<Map<String, Object>> logs(HttpSession session, @PathVariable String type) {
        access.requireAdmin(session);
        return admin.logs(type);
    }

    @GetMapping("/admin/reviews")
    public List<Map<String, Object>> reviews(HttpSession session) {
        access.requireAdmin(session);
        return admin.reviews();
    }

    @GetMapping("/admin/stats")
    public Map<String, Object> stats(HttpSession session) {
        access.requireAdmin(session);
        return admin.stats();
    }

    @GetMapping("/instructor/courses/{id}")
    public Map<String, Object> curriculum(HttpSession session, @PathVariable int id) {
        access.requireAdmin(session);
        return admin.fullCourse(id);
    }

    @PostMapping("/courses/{id}/sections")
    public ResponseEntity<Void> addSection(
            HttpSession session, @PathVariable int id,
            @RequestBody(required = false) Map<String, Object> body) {
        access.requireAdmin(session);
        admin.addSection(id, bodyOrEmpty(body));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Void> deleteSection(HttpSession session, @PathVariable int id) {
        access.requireAdmin(session);
        admin.deleteSection(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sections/{id}/lessons")
    public ResponseEntity<Void> addLesson(
            HttpSession session, @PathVariable int id,
            @RequestBody(required = false) Map<String, Object> body) {
        access.requireAdmin(session);
        admin.addLesson(id, bodyOrEmpty(body));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(HttpSession session, @PathVariable int id) {
        access.requireAdmin(session);
        admin.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/sections/{id}/quiz")
    public Map<String, Object> saveQuiz(
            HttpSession session, @PathVariable int id,
            @RequestBody(required = false) Map<String, Object> body) {
        access.requireAdmin(session);
        return admin.saveQuiz(id, bodyOrEmpty(body));
    }

    @DeleteMapping("/sections/{id}/quiz")
    public ResponseEntity<Void> deleteQuiz(HttpSession session, @PathVariable int id) {
        access.requireAdmin(session);
        admin.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/support")
    public Map<String, Object> createSupportTicket(
            HttpSession session, @RequestBody(required = false) Map<String, Object> body) {
        Object value = session.getAttribute("user");
        User user = value instanceof User currentUser ? currentUser : null;
        return admin.addSupportTicket(bodyOrEmpty(body), user);
    }

    private Map<String, Object> bodyOrEmpty(Map<String, Object> body) {
        return body == null ? Map.of() : body;
    }
}
