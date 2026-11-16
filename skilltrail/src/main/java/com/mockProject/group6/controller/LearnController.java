package com.mockProject.group6.controller;

import com.mockProject.group6.security.Auth;
import com.mockProject.group6.service.LearnService;
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

/** Student calls: enrollments, progress, quizzes, reviews and the shopping cart. */
@RestController
@RequestMapping("/api")
public class LearnController {

    private final Auth auth;
    private final LearnService learn;

    public LearnController(Auth auth, LearnService learn) {
        this.auth = auth;
        this.learn = learn;
    }

    @GetMapping("/enrollments/me")
    public ResponseEntity<Object> enrollments(HttpServletRequest req) {
        return Http.ok(learn.myEnrollments(auth.require(req)));
    }

    @PostMapping("/courses/{id}/enroll")
    public ResponseEntity<Object> enroll(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        return Http.ok(learn.enroll(auth.require(req), id, In.body(body)));
    }

    @DeleteMapping("/courses/{id}/enroll")
    public ResponseEntity<Object> unenroll(HttpServletRequest req, @PathVariable("id") int id) {
        learn.unenroll(auth.require(req), id);
        return Http.none();
    }

    @PutMapping("/courses/{id}/progress")
    public ResponseEntity<Object> progress(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        return Http.ok(learn.setProgress(auth.require(req), id, In.body(body)));
    }

    @GetMapping("/quizzes/{id}")
    public ResponseEntity<Object> quiz(HttpServletRequest req, @PathVariable("id") int id) {
        return Http.ok(learn.quiz(auth.require(req), id));
    }

    @PostMapping("/quizzes/{id}/attempts")
    public ResponseEntity<Object> attempt(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        return Http.ok(learn.attempt(auth.require(req), id, In.body(body)));
    }

    @PostMapping("/courses/{id}/reviews")
    public ResponseEntity<Object> addReview(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        return Http.ok(learn.addReview(auth.require(req), id, In.body(body)));
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<Object> editReview(HttpServletRequest req, @PathVariable("id") int id, @RequestBody(required = false) Map<String, Object> body) {
        return Http.ok(learn.editReview(auth.require(req), id, In.body(body)));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Object> deleteReview(HttpServletRequest req, @PathVariable("id") int id) {
        learn.deleteReview(auth.require(req), id);
        return Http.none();
    }

    @GetMapping("/cart")
    public ResponseEntity<Object> cart(HttpServletRequest req) {
        return Http.ok(learn.cart(auth.require(req)));
    }

    @PostMapping("/cart/{courseId}")
    public ResponseEntity<Object> addToCart(HttpServletRequest req, @PathVariable("courseId") int courseId) {
        return Http.ok(learn.addToCart(auth.require(req), courseId));
    }

    @DeleteMapping("/cart/{courseId}")
    public ResponseEntity<Object> removeFromCart(HttpServletRequest req, @PathVariable("courseId") int courseId) {
        learn.removeFromCart(auth.require(req), courseId);
        return Http.none();
    }

    @DeleteMapping("/cart")
    public ResponseEntity<Object> clearCart(HttpServletRequest req) {
        learn.clearCart(auth.require(req));
        return Http.none();
    }

    @PostMapping("/cart/checkout")
    public ResponseEntity<Object> checkout(HttpServletRequest req, @RequestBody(required = false) Map<String, Object> body) {
        return Http.ok(learn.checkout(auth.require(req), In.body(body)));
    }
}
