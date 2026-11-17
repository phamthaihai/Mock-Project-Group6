package com.mockProject.group6.service;

import com.mockProject.group6.entity.User;
import com.mockProject.group6.entity.UserStatus;
import com.mockProject.group6.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class AdminAccess {

    private final UserRepository users;

    public AdminAccess(UserRepository users) {
        this.users = users;
    }

    public User requireAdmin(HttpSession session) {
        Object value = session.getAttribute("user");
        if (!(value instanceof User sessionUser)) {
            throw new AdminApiException(401, "Please log in first.");
        }

        User user = users.findById(sessionUser.getId())
                .orElseThrow(() -> new AdminApiException(401, "Please log in first."));

        if (user.getStatus() != UserStatus.ACTIVE || Boolean.TRUE.equals(user.getLocked())) {
            throw new AdminApiException(403, "This account is not active.");
        }

        String role = user.getRole() == null ? "" : user.getRole().getName();
        if (!"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role)) {
            throw new AdminApiException(403, "Admin access is required.");
        }

        return user;
    }
}
