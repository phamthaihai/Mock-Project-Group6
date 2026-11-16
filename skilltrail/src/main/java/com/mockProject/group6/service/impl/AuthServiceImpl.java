package com.mockProject.group6.service.impl;

import com.mockProject.group6.dto.RegisterRequest;
import com.mockProject.group6.dto.RegistrationResult;
import com.mockProject.group6.entity.User;
import com.mockProject.group6.exception.ApiException;
import com.mockProject.group6.repository.AuthRepository;
import com.mockProject.group6.service.ActivityService;
import com.mockProject.group6.service.AuthService;
import com.mockProject.group6.service.EmailVerificationService;
import com.mockProject.group6.validation.SignupValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepository repo;
    private final SignupValidator validator;
    private final PasswordEncoder encoder;
    private final EmailVerificationService verification;
    private final ActivityService activity;

    public AuthServiceImpl(AuthRepository repo, SignupValidator validator, PasswordEncoder encoder,
                           EmailVerificationService verification, ActivityService activity) {
        this.repo = repo;
        this.validator = validator;
        this.encoder = encoder;
        this.verification = verification;
        this.activity = activity;
    }

    @Override
    @Transactional
    public RegistrationResult register(RegisterRequest req) {
        Map<String, String> errors = validator.validate(req);
        if (!errors.isEmpty()) {
            throw new ApiException(400, errors.values().iterator().next(), errors);
        }
        String email = req.email().strip();
        User u = repo.findByEmailIgnoreCase(email).orElse(null);
        if (u != null && u.isVerified()) {
            throw new ApiException(409, "Email already registered");
        }
        boolean isNew = u == null;
        if (u == null) { // an unverified sign-up for this email (typo, lost code) is simply redone
            u = new User();
            u.setEmail(email);
            u.setRole("STUDENT");
        }
        u.setFullName(req.fullName().strip());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setVerified(false);
        try {
            u = repo.saveAndFlush(u);
        } catch (DataIntegrityViolationException e) { // two sign-ups with the same email at once
            throw new ApiException(409, "Email already registered");
        }
        if (isNew) activity.account("REGISTERED", u.getUserId(), email, "Email sign-up (awaiting email verification)", u.getFullName());
        return new RegistrationResult(u, verification.issue(email));
    }

    @Override
    @Transactional(noRollbackFor = ApiException.class) // a wrong code must still count as a try
    public void verifyEmail(String email, String code) {
        String e = email == null ? "" : email.strip();
        User u = repo.findByEmailIgnoreCase(e).filter(x -> !x.isVerified())
                .orElseThrow(() -> new ApiException(400, "Verification code is invalid or expired"));
        verification.confirm(e, code);
        u.setVerified(true);
        repo.save(u);
    }

    @Override
    @Transactional
    public void resendCode(String email) {
        String e = email == null ? "" : email.strip();
        repo.findByEmailIgnoreCase(e).filter(x -> !x.isVerified()).ifPresent(u -> verification.issue(e));
    }
}
