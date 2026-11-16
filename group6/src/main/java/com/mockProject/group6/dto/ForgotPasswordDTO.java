package com.mockProject.group6.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}