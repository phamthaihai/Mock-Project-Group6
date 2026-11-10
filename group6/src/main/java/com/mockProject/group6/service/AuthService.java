package com.mockProject.group6.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.mockProject.group6.dto.RegisterDTO;
import com.mockProject.group6.entity.Role;
import com.mockProject.group6.entity.User;
import com.mockProject.group6.entity.UserStatus;
import com.mockProject.group6.repository.RoleRepository;
import com.mockProject.group6.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;

    public AuthService(UserRepository userRepo,
                       RoleRepository roleRepo,
                       BCryptPasswordEncoder encoder,
                       EmailService emailService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    @Transactional
    public void register(RegisterDTO dto, String baseUrl) {

        String email = dto.getEmail().trim().toLowerCase();

        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        String fullName = dto.getFullname().trim();
        if (userRepo.existsByFullNameIgnoreCase(fullName)) {
            throw new IllegalArgumentException("Full name already exists");
        }

        Role memberRole = roleRepo.findByName("MEMBER")
                .or(() -> roleRepo.findByName("USER"))
                .or(() -> roleRepo.findByName("ADMIN"))
                .orElseThrow(() -> new IllegalArgumentException("No default role found for new users. Check seed role."));

        String token = UUID.randomUUID().toString().replace("-", "");

        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        user.setRole(memberRole);
        user.setPasswordHash(encoder.encode(dto.getPassword()));
        user.setStatus(UserStatus.UNVERIFIED);
        user.setEmailVerified(false);
        user.setVerifyToken(token);
        user.setUpdatedAt(LocalDateTime.now());
        user.setVerifyTokenExpiresAt(LocalDateTime.now().plusMinutes(30));

        userRepo.save(user);

        String verifyLink = baseUrl + "/verify?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verifyLink);
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepo.findByVerifyToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token."));

        if (user.getVerifyTokenExpiresAt() == null
                || user.getVerifyTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired.");
        }

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setVerifyToken(null);
        user.setVerifyTokenExpiresAt(null);
        user.setUpdatedAt(LocalDateTime.now());

        userRepo.save(user);
    }
}