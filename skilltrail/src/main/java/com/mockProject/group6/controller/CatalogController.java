package com.mockProject.group6.controller;

import com.mockProject.group6.exception.ApiException;
import com.mockProject.group6.security.Auth;
import com.mockProject.group6.security.AuthUser;
import com.mockProject.group6.service.AdminService;
import com.mockProject.group6.service.CourseService;
import com.mockProject.group6.service.LearnService;
import com.mockProject.group6.support.Http;
import com.mockProject.group6.support.In;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Public catalog (no login needed) plus the support request form. */
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final Auth auth;
    private final CourseService courses;
    private final LearnService learn;
    private final AdminService admin;

    public CatalogController(Auth auth, CourseService courses, LearnService learn, AdminService admin) {
        this.auth = auth;
        this.courses = courses;
        this.learn = learn;
        this.admin = admin;
    }

    @GetMapping("/categories")
    public ResponseEntity<Object> categories() {
        return Http.ok(admin.categories());
    }

    @GetMapping("/courses")
    public ResponseEntity<Object> courses() {
        return Http.ok(courses.views("WHERE c.Status = 'PUBLISHED'"));
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<Object> course(HttpServletRequest req, @PathVariable("id") int id) {
        Map<String, Object> c = courses.row(id);
        if (!"PUBLISHED".equals(String.valueOf(c.get("Status"))) && !courses.canSeeUnpublished(auth.current(req), c)) {
            throw new ApiException(404, "Course not found");
        }
        return Http.ok(courses.viewOrFail(id));
    }

    @GetMapping("/courses/{id}/reviews")
    public ResponseEntity<Object> reviews(@PathVariable("id") int id) {
        return Http.ok(learn.reviews(id));
    }

    @GetMapping("/courses/{id}/progress")
    public ResponseEntity<Object> progress(HttpServletRequest req, @PathVariable("id") int id) {
        courses.row(id);
        AuthUser me = auth.current(req);
        return Http.ok(courses.progress(id, me == null ? null : me.userId()));
    }

    @GetMapping("/lessons/{id}")
    public ResponseEntity<Object> lesson(HttpServletRequest req, @PathVariable("id") int id) {
        return Http.ok(courses.lesson(id, auth.current(req)));
    }

    /** Guests can write too. */
    @PostMapping("/support")
    public ResponseEntity<Object> support(HttpServletRequest req, @RequestBody(required = false) Map<String, Object> body) {
        return Http.ok(learn.createTicket(auth.current(req), In.body(body)));
    }

    @GetMapping("/support/me")
    public ResponseEntity<Object> mySupport(HttpServletRequest req) {
        return Http.ok(learn.myTickets(auth.require(req)));
    }
}
