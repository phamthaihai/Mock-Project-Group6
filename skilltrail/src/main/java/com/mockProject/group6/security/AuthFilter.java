package com.mockProject.group6.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Reads "Authorization: Bearer ..." on /api calls and stores the user on the request. A missing or bad token just means "not logged in". */
@Component
public class AuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final JdbcTemplate jdbc;

    public AuthFilter(JwtService jwt, JdbcTemplate jdbc) {
        this.jwt = jwt;
        this.jdbc = jdbc;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String h = request.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            Integer id = jwt.verify(h.substring(7).trim());
            if (id != null) {
                List<Map<String, Object>> rows = jdbc.queryForList(
                        "SELECT UserId, FullName, Email, Role, Locked FROM Users WHERE UserId = ?", id);
                if (!rows.isEmpty()) {
                    Map<String, Object> u = rows.get(0);
                    boolean locked = Boolean.TRUE.equals(u.get("Locked"));
                    request.setAttribute(Auth.ATTR, new AuthUser(((Number) u.get("UserId")).intValue(),
                            String.valueOf(u.get("FullName")), String.valueOf(u.get("Email")), String.valueOf(u.get("Role")), locked));
                }
            }
        }
        chain.doFilter(request, response);
    }
}
