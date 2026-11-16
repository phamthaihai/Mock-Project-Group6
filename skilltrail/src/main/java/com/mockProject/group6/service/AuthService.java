package com.mockProject.group6.service;

import com.mockProject.group6.dto.RegisterRequest;
import com.mockProject.group6.dto.RegistrationResult;

public interface AuthService {

    /** Validates, hashes the password, saves an UNVERIFIED STUDENT and emails a code. 400 invalid, 409 email exists. */
    RegistrationResult register(RegisterRequest request);

    /** Marks the account verified when the code is right. 400 if the code is wrong or expired. */
    void verifyEmail(String email, String code);

    /** Sends a new code to an unverified account. Silent when the email is unknown (no account enumeration). 429 if too soon. */
    void resendCode(String email);
}
