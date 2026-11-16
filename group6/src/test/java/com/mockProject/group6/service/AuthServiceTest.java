package com.mockProject.group6.service;

import com.mockProject.group6.dto.RegisterDTO;
import com.mockProject.group6.entity.Role;
import com.mockProject.group6.entity.User;
import com.mockProject.group6.entity.UserStatus;
import com.mockProject.group6.repository.RoleRepository;
import com.mockProject.group6.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Test
    void registerCreatesUnverifiedUserAndSendsVerificationEmail() {
        UserRepository userRepo = mock(UserRepository.class);
        RoleRepository roleRepo = mock(RoleRepository.class);
        EmailService emailService = mock(EmailService.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        AuthService authService = new AuthService(userRepo, roleRepo, encoder, emailService);

        Role memberRole = new Role();
        memberRole.setName("MEMBER");

        when(userRepo.existsByEmail("alice@example.com")).thenReturn(false);
        when(roleRepo.findByName("MEMBER")).thenReturn(Optional.of(memberRole));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterDTO dto = new RegisterDTO();
        dto.setFullname("Alice Nguyen");
        dto.setEmail("alice@example.com");
        dto.setPhone("0912345678");
        dto.setPassword("StrongPass!123");
        dto.setConfirmPassword("StrongPass!123");

        authService.register(dto, "http://localhost:8080");

        verify(emailService).sendVerificationEmail(eq("alice@example.com"), eq("Alice Nguyen"), anyString());
        verify(userRepo).save(any(User.class));
    }

    @Test
    void verifyEmailActivatesUserAndClearsToken() {
        UserRepository userRepo = mock(UserRepository.class);
        RoleRepository roleRepo = mock(RoleRepository.class);
        EmailService emailService = mock(EmailService.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        AuthService authService = new AuthService(userRepo, roleRepo, encoder, emailService);

        User user = new User();
        user.setId(7);
        user.setEmail("bob@example.com");
        user.setFullName("Bob");
        user.setStatus(UserStatus.UNVERIFIED);
        user.setVerifyToken("valid-token");
        user.setVerifyTokenExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));

        when(userRepo.findByVerifyToken("valid-token")).thenReturn(Optional.of(user));

        authService.verifyEmail("valid-token");

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNull(user.getVerifyToken());
        assertNull(user.getVerifyTokenExpiresAt());
        verify(userRepo).save(user);
    }
}
