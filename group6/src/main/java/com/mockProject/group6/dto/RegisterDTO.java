package com.mockProject.group6.dto;

import jakarta.validation.constraints.*;

public class RegisterDTO {
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullname;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 7, message = "Password must be greater than 6 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @AssertTrue(message = "Confirm password does not match")
    public boolean isPasswordMatch() {
        if (password == null || confirmPassword == null || confirmPassword.isBlank()) return true;
        return password.equals(confirmPassword);
    }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}