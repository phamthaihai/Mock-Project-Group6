package com.mockProject.group6.controller;

import com.mockProject.group6.exception.ApiException;
import com.mockProject.group6.security.Auth;
import com.mockProject.group6.security.AuthUser;
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

/** Course editor (sections, lessons, quiz). Only admins manage courses; the "instructor" role no longer exists. */
@RestController
@RequestMapping("/api")
public class ManageController {

    private final Auth auth;
    private final CourseService courses;

    public ManageController(Auth auth, CourseService courses) {
        this.auth = auth;
        this.courses = courses;
    }

    /** Logged in, the course exists, and the caller may manage it. */
    private AuthUser manageCourse(HttpServletRequest req, int courseId) {
        AuthUser me = auth.require(req);
        courses.row(courseId);
        if (!me.admin()) throw new ApiException(403, "You can only manage your own courses");
        return me;
    }

    @GetMapping("/instructor/courses")
    public ResponseEntity<Object> list(HttpServletRequest req) {
        auth.admin(req);
        return Http.ok(courses.views(""));
    }

    @GetMapping("/instructor/courses/{id}")
    public ResponseEntity<Object> one(HttpServletRequest req, @PathVariable("id") int id) {
        manageCourse(req, id);
        return Http.ok(courses.fullCourse(id));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<Object> update(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        manageCourse(req, id);
        return Http.ok(courses.update(id, In.body(body)));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Object> delete(HttpServletRequest req, @PathVariable("id") int id) {
        manageCourse(req, id);
        courses.delete(id);
        return Http.none();
    }

    @PostMapping("/courses/{id}/submit")
    public ResponseEntity<Object> submit(HttpServletRequest req, @PathVariable("id") int id) {
        AuthUser me = manageCourse(req, id);
        return Http.ok(courses.submit(me, id));
    }

    @GetMapping("/courses/{id}/students")
    public ResponseEntity<Object> students(HttpServletRequest req, @PathVariable("id") int id) {
        manageCourse(req, id);
        return Http.ok(courses.students(id));
    }

    @PostMapping("/courses/{id}/sections")
    public ResponseEntity<Object> addSection(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        manageCourse(req, id);
        return Http.ok(courses.addSection(id, In.body(body)));
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<Object> renameSection(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        manageCourse(req, courses.courseOfSection(id));
        return Http.ok(courses.renameSection(id, In.body(body)));
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Object> deleteSection(HttpServletRequest req, @PathVariable("id") int id) {
        manageCourse(req, courses.courseOfSection(id));
        courses.deleteSection(id);
        return Http.none();
    }

    @PostMapping("/sections/{id}/move")
    public ResponseEntity<Object> moveSection(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        manageCourse(req, courses.courseOfSection(id));
        courses.moveSection(id, In.body(body));
        return Http.none();
    }

    @PostMapping("/sections/{id}/lessons")
    public ResponseEntity<Object> addLesson(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        manageCourse(req, courses.courseOfSection(id));
        return Http.ok(courses.addLesson(id, In.body(body)));
    }

    @PutMapping("/lessons/{id}")
    public ResponseEntity<Object> updateLesson(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        manageCourse(req, courses.courseOfLesson(id));
        return Http.ok(courses.updateLesson(id, In.body(body)));
    }

    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Object> deleteLesson(HttpServletRequest req, @PathVariable("id") int id) {
        manageCourse(req, courses.courseOfLesson(id));
        courses.deleteLesson(id);
        return Http.none();
    }

    @PostMapping("/lessons/{id}/move")
    public ResponseEntity<Object> moveLesson(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        manageCourse(req, courses.courseOfLesson(id));
        courses.moveLesson(id, In.body(body));
        return Http.none();
    }

    @PutMapping("/sections/{id}/quiz")
    public ResponseEntity<Object> saveQuiz(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        manageCourse(req, courses.courseOfSection(id));
        courses.saveQuiz(id, In.body(body));
        return Http.none();
    }

    @DeleteMapping("/sections/{id}/quiz")
    public ResponseEntity<Object> deleteQuiz(HttpServletRequest req, @PathVariable("id") int id) {
        manageCourse(req, courses.courseOfSection(id));
        courses.deleteQuiz(id);
        return Http.none();
    }
}
