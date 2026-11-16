package com.mockProject.group6.security;

import com.mockProject.group6.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/** Who is calling? Controllers use current() for public endpoints, require() for logged-in users and admin() for admin-only ones. */
@Component
public class Auth {

    public static final String ATTR = "skilltrail.user";

    /** The logged-in user or null (guest). */
    public AuthUser current(HttpServletRequest r) {
        Object u = r.getAttribute(ATTR);
        return u instanceof AuthUser ? (AuthUser) u : null;
    }

    public AuthUser require(HttpServletRequest r) {
        AuthUser u = current(r);
        if (u == null) throw new ApiException(401, "Please log in first");
        if (u.locked()) throw new ApiException(403, "This account has been locked");
        return u;
    }

    public AuthUser admin(HttpServletRequest r) {
        AuthUser u = require(r);
        if (!u.admin()) throw new ApiException(403, "Admin only");
        return u;
    }
}
