package com.mockProject.group6.controller;

import com.mockProject.group6.security.Auth;
import com.mockProject.group6.security.AuthUser;
import com.mockProject.group6.service.AdminService;
import com.mockProject.group6.service.CourseService;
import com.mockProject.group6.support.Http;
import com.mockProject.group6.support.In;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Admin only: course approval, categories, users, support inbox, logs and dashboard. */
@RestController
@RequestMapping("/api")
public class AdminController {

    private final Auth auth;
    private final AdminService admin;
    private final CourseService courses;

    public AdminController(Auth auth, AdminService admin, CourseService courses) {
        this.auth = auth;
        this.admin = admin;
        this.courses = courses;
    }

    /* courses */

    @GetMapping("/admin/courses")
    public ResponseEntity<Object> courseList(HttpServletRequest req) {
        auth.admin(req);
        return Http.ok(courses.views(""));
    }

    @PostMapping("/admin/courses")
    public ResponseEntity<Object> createCourse(HttpServletRequest req, @RequestBody(required = false) Map<String, Object> body) {
        auth.admin(req);
        return Http.ok(courses.adminCreate(In.body(body)));
    }

    @PutMapping("/admin/courses/{id}")
    public ResponseEntity<Object> updateCourse(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        auth.admin(req);
        return Http.ok(courses.adminUpdate(id, In.body(body)));
    }

    @DeleteMapping("/admin/courses/{id}")
    public ResponseEntity<Object> deleteCourse(HttpServletRequest req, @PathVariable("id") int id) {
        auth.admin(req);
        courses.adminDelete(id);
        return Http.none();
    }

    @PostMapping("/admin/courses/{id}/approve")
    public ResponseEntity<Object> approve(HttpServletRequest req, @PathVariable("id") int id) {
        auth.admin(req);
        return Http.ok(courses.approve(id));
    }

    @PostMapping("/admin/courses/{id}/reject")
    public ResponseEntity<Object> reject(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        auth.admin(req);
        return Http.ok(courses.reject(id, In.body(body)));
    }

    /* categories */

    @PostMapping("/categories")
    public ResponseEntity<Object> addCategory(HttpServletRequest req, @RequestBody(required = false) Map<String, Object> body) {
        auth.admin(req);
        return Http.ok(admin.addCategory(In.body(body)));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Object> renameCategory(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        auth.admin(req);
        return Http.ok(admin.renameCategory(id, In.body(body)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Object> deleteCategory(HttpServletRequest req, @PathVariable("id") int id) {
        auth.admin(req);
        admin.deleteCategory(id);
        return Http.none();
    }

    /* users */

    @GetMapping("/admin/users")
    public ResponseEntity<Object> users(HttpServletRequest req) {
        auth.admin(req);
        return Http.ok(admin.users());
    }

    @PutMapping("/admin/users/{id}/role")
    public ResponseEntity<Object> role(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        AuthUser me = auth.admin(req);
        admin.changeRole(me, id, In.body(body));
        return Http.none();
    }

    @PutMapping("/admin/users/{id}/lock")
    public ResponseEntity<Object> lock(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        AuthUser me = auth.admin(req);
        admin.lock(me, id, In.body(body));
        return Http.none();
    }

    /* support inbox, logs, reviews, dashboard */

    @GetMapping("/admin/tickets")
    public ResponseEntity<Object> tickets(HttpServletRequest req) {
        auth.admin(req);
        return Http.ok(admin.tickets());
    }

    @PutMapping("/admin/tickets/{id}")
    public ResponseEntity<Object> updateTicket(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        auth.admin(req);
        return Http.ok(admin.updateTicket(id, In.body(body)));
    }

    @GetMapping("/admin/logs/{type}")
    public ResponseEntity<Object> logs(HttpServletRequest req, @PathVariable("type") String type) {
        auth.admin(req);
        if (!type.matches("(?i)account|payment|enrollment|completion")) throw new com.mockProject.group6.exception.ApiException(404, "Not found");
        return Http.ok(admin.logs(type));
    }

    @GetMapping("/admin/reviews")
    public ResponseEntity<Object> reviews(HttpServletRequest req) {
        auth.admin(req);
        return Http.ok(admin.reviews());
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<Object> stats(HttpServletRequest req) {
        auth.admin(req);
        return Http.ok(admin.stats());
    }
}
