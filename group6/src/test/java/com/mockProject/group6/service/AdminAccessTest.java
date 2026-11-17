package com.mockProject.group6.service;

import com.mockProject.group6.entity.Role;
import com.mockProject.group6.entity.User;
import com.mockProject.group6.entity.UserStatus;
import com.mockProject.group6.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminAccessTest {

    private UserRepository users;
    private AdminAccess access;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        access = new AdminAccess(users);
    }

    @Test
    void allowsActiveAdminFromTheCurrentDatabaseRecord() {
        User sessionUser = user(12, "USER", UserStatus.ACTIVE);
        User databaseAdmin = user(12, "ADMIN", UserStatus.ACTIVE);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", sessionUser);
        when(users.findById(12)).thenReturn(Optional.of(databaseAdmin));

        assertSame(databaseAdmin, access.requireAdmin(session));
    }

    @Test
    void rejectsAdminSessionAfterRoleWasRemoved() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user(12, "ADMIN", UserStatus.ACTIVE));
        when(users.findById(12)).thenReturn(Optional.of(user(12, "USER", UserStatus.ACTIVE)));

        AdminApiException exception = assertThrows(
                AdminApiException.class, () -> access.requireAdmin(session));

        assertEquals(403, exception.getStatus());
    }

    @Test
    void rejectsLockedAdminAccount() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", user(12, "ADMIN", UserStatus.ACTIVE));
        User lockedAdmin = user(12, "ADMIN", UserStatus.ACTIVE);
        lockedAdmin.setLocked(true);
        when(users.findById(12)).thenReturn(Optional.of(lockedAdmin));

        AdminApiException exception = assertThrows(
                AdminApiException.class, () -> access.requireAdmin(session));

        assertEquals(403, exception.getStatus());
    }

    @Test
    void requiresLogin() {
        AdminApiException exception = assertThrows(
                AdminApiException.class, () -> access.requireAdmin(new MockHttpSession()));

        assertEquals(401, exception.getStatus());
    }

    private User user(int id, String roleName, UserStatus status) {
        Role role = new Role();
        role.setName(roleName);
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
