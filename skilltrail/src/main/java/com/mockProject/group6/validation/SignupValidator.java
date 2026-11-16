package com.mockProject.group6.validation;

import com.mockProject.group6.dto.RegisterRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Same rules and messages as validateSignup() in static/js/common.js.
 * Returns {field -> message} in the order fullName, email, password. Empty map = valid.
 */
@Component
public class SignupValidator {

    private static final Pattern NAME = Pattern.compile("^\\p{L}[\\p{L} .'\\-]*$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern LETTER = Pattern.compile("[A-Za-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPACE = Pattern.compile("\\s");

    public Map<String, String> validate(RegisterRequest r) {
        Map<String, String> errors = new LinkedHashMap<>();
        String name = fullName(r == null ? null : r.fullName());
        String email = email(r == null ? null : r.email());
        String pass = password(r == null ? null : r.password());
        if (name != null) errors.put("fullName", name);
        if (email != null) errors.put("email", email);
        if (pass != null) errors.put("password", pass);
        return errors;
    }

    private String fullName(String raw) {
        String v = raw == null ? "" : raw.strip();
        if (v.isEmpty()) return "Full name is required";
        if (v.length() < 2) return "Full name must be at least 2 characters";
        if (v.length() > 100) return "Full name must be at most 100 characters";
        if (!NAME.matcher(v).matches()) return "Full name can only contain letters, spaces, . ' and -";
        return null;
    }

    private String email(String raw) {
        String v = raw == null ? "" : raw.strip();
        if (v.isEmpty()) return "Email is required";
        if (v.length() > 150) return "Email must be at most 150 characters";
        if (!EMAIL.matcher(v).matches()) return "Enter a valid email, for example name@gmail.com";
        return null;
    }

    private String password(String v) {
        if (v == null || v.isEmpty()) return "Password is required";
        if (v.length() < 6) return "Password must be at least 6 characters";
        if (v.length() > 64) return "Password must be at most 64 characters";
        if (SPACE.matcher(v).find()) return "Password cannot contain spaces";
        if (!LETTER.matcher(v).find() || !DIGIT.matcher(v).find()) return "Password needs at least one letter and one number";
        return null;
    }
}
