package com.mockProject.group6.controller;

import com.mockProject.group6.security.Auth;
import com.mockProject.group6.security.AuthUser;
import com.mockProject.group6.service.AccountService;
import com.mockProject.group6.support.Http;
import com.mockProject.group6.support.In;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** My account and my notifications. */
@RestController
@RequestMapping("/api")
public class UserController {

    private final Auth auth;
    private final AccountService accounts;

    public UserController(Auth auth, AccountService accounts) {
        this.auth = auth;
        this.accounts = accounts;
    }

    @GetMapping("/users/me")
    public ResponseEntity<Object> me(HttpServletRequest req) {
        return Http.ok(accounts.me(auth.require(req)));
    }

    @PutMapping("/users/me")
    public ResponseEntity<Object> update(HttpServletRequest req, @RequestBody(required = false) Map<String, Object> body) {
        return Http.ok(accounts.updateMe(auth.require(req), In.body(body)));
    }

    @PutMapping("/users/me/password")
    public ResponseEntity<Object> password(HttpServletRequest req, @RequestBody(required = false) Map<String, Object> body) {
        accounts.changePassword(auth.require(req), In.body(body));
        return Http.none();
    }

    @PostMapping("/users/me/google/unbind")
    public ResponseEntity<Object> unbind(HttpServletRequest req, @RequestBody(required = false) Map<String, Object> body) {
        accounts.unbindGoogle(auth.require(req), In.body(body));
        return Http.none();
    }

    @GetMapping("/notifications/me")
    public ResponseEntity<Object> notifications(HttpServletRequest req) {
        AuthUser me = auth.require(req);
        return Http.ok(accounts.notifications(me));
    }

    @PostMapping("/notifications/read")
    public ResponseEntity<Object> read(HttpServletRequest req) {
        accounts.markNotificationsRead(auth.require(req));
        return Http.none();
    }
}
