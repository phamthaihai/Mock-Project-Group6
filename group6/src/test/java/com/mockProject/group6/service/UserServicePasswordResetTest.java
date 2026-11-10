package com.mockProject.group6.service;

import com.mockProject.group6.entity.User;
import com.mockProject.group6.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServicePasswordResetTest {

    @Test
    void createsHashedTenMinuteTokenAndConsumesItAfterReset() {
        UserRepository userRepository = mock(UserRepository.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UserService userService = createService(userRepository, encoder);
        User user = new User();
        LocalDateTime beforeRequest = LocalDateTime.now();

        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findByPasswordResetTokenHash(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = userService.createPasswordResetToken(" Alice@Example.com ").orElseThrow();

        assertEquals(43, token.length());
        assertEquals(64, user.getPasswordResetTokenHash().length());
        assertNotEquals(token, user.getPasswordResetTokenHash());
        assertTrue(user.getPasswordResetTokenExpiresAt().isAfter(beforeRequest.plusMinutes(9)));
        assertTrue(user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now().plusMinutes(10).plusSeconds(1)));
        assertTrue(userService.isPasswordResetTokenValid(token));

        userService.resetPassword(token, "newPassword123");

        assertTrue(encoder.matches("newPassword123", user.getPasswordHash()));
        assertTrue(user.getPasswordResetTokenHash() == null);
        assertTrue(user.getPasswordResetTokenExpiresAt() == null);
        assertFalse(userService.isPasswordResetTokenValid(token));
        verify(userRepository, org.mockito.Mockito.times(2)).save(user);
    }

    @Test
    void doesNotIssueTokenForUnknownEmail() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = createService(userRepository, new BCryptPasswordEncoder());
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertTrue(userService.createPasswordResetToken("missing@example.com").isEmpty());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectsExpiredResetToken() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = createService(userRepository, new BCryptPasswordEncoder());
        User user = new User();
        user.setPasswordResetTokenHash("hash");
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(userRepository.findByPasswordResetTokenHash(anyString())).thenReturn(Optional.of(user));

        assertFalse(userService.isPasswordResetTokenValid("expired-token"));
        assertThrows(IllegalArgumentException.class,
                () -> userService.resetPassword("expired-token", "newPassword123"));
    }

    private UserService createService(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        UserService userService = new UserService();
        ReflectionTestUtils.setField(userService, "userRepository", userRepository);
        ReflectionTestUtils.setField(userService, "encoder", encoder);
        return userService;
    }
}