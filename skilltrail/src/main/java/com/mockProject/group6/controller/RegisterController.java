package com.mockProject.group6.controller;

import com.mockProject.group6.dto.RegisterRequest;
import com.mockProject.group6.dto.RegistrationResult;
import com.mockProject.group6.entity.User;
import com.mockProject.group6.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RegisterController {

    public record VerifyRequest(String email, String code) {}

    public record ResendRequest(String email) {}

    private final AuthService authService;

    public RegisterController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        RegistrationResult r = authService.register(request);
        User u = r.user();
        return ResponseEntity.status(201).body(Map.of(
                "userId", u.getUserId(),
                "fullName", u.getFullName(),
                "email", u.getEmail(),
                "role", u.getRole(),
                "verificationRequired", true,
                "emailSent", r.emailSent()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestBody VerifyRequest request) {
        authService.verifyEmail(request.email(), request.code());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-code")
    public Map<String, String> resend(@RequestBody ResendRequest request) {
        authService.resendCode(request.email());
        return Map.of("message", "If that email is waiting for verification, a new code has been sent.");
    }
}
