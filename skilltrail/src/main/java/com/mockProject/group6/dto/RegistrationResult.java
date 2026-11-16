package com.mockProject.group6.dto;

import com.mockProject.group6.entity.User;

/** emailSent = false when the SMTP server refused the message; the user can press "Resend code". */
public record RegistrationResult(User user, boolean emailSent) {
}
