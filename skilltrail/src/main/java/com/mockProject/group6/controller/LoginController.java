package com.mockProject.group6.controller;

import com.mockProject.group6.service.AccountService;
import com.mockProject.group6.support.Http;
import com.mockProject.group6.support.In;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Login, Google sign-in and forgot / reset password. (Register and email verification are in RegisterController.) */
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final AccountService accounts;

    public LoginController(AccountService accounts) {
        this.accounts = accounts;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody(required = false) Map<String, Object> body) {
        return Http.ok(accounts.login(In.body(body)));
    }

    @PostMapping("/google")
    public ResponseEntity<Object> google(@RequestBody(required = false) Map<String, Object> body) {
        return Http.ok(accounts.google(In.body(body)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgot(@RequestBody(required = false) Map<String, Object> body) {
        return Http.ok(accounts.forgotPassword(In.body(body)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Object> reset(@RequestBody(required = false) Map<String, Object> body) {
        accounts.resetPassword(In.body(body));
        return Http.none();
    }
}
