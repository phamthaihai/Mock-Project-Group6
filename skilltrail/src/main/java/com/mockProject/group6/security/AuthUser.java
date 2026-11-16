package com.mockProject.group6.security;

/** The logged-in user of the current request (loaded from the database on every call, so locks and role changes apply at once). */
public record AuthUser(int userId, String fullName, String email, String role, boolean locked) {

    public boolean admin() {
        return "ADMIN".equals(role);
    }
}
