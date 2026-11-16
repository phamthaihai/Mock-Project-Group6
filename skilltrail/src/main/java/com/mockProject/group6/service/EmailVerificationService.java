package com.mockProject.group6.service;

import com.mockProject.group6.entity.EmailVerification;
import com.mockProject.group6.exception.ApiException;
import com.mockProject.group6.repository.EmailVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

/** 6-digit code, stored as a BCrypt hash, valid 10 minutes, 5 wrong tries, 60 seconds between sends. */
@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final int EXPIRY_MINUTES = 10, MAX_TRIES = 5, COOLDOWN_SECONDS = 60;

    private final SecureRandom random = new SecureRandom();
    private final EmailVerificationRepository repo;
    private final MailService mail;
    private final PasswordEncoder encoder;

    public EmailVerificationService(EmailVerificationRepository repo, MailService mail, PasswordEncoder encoder) {
        this.repo = repo;
        this.mail = mail;
        this.encoder = encoder;
    }

    /** Creates or replaces the code for this email and mails it. Returns false if the email could not be sent. */
    public boolean issue(String email) {
        LocalDateTime now = LocalDateTime.now();
        EmailVerification v = repo.findByEmailIgnoreCase(email).orElseGet(EmailVerification::new);
        if (v.getSentAt() != null) {
            long left = COOLDOWN_SECONDS - Duration.between(v.getSentAt(), now).getSeconds();
            if (left > 0) throw new ApiException(429, "Please wait " + left + " seconds before requesting a new code");
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        v.setEmail(email);
        v.setCodeHash(encoder.encode(code));
        v.setExpiresAt(now.plusMinutes(EXPIRY_MINUTES));
        v.setTries(0);
        v.setSentAt(now);
        repo.save(v);
        try {
            mail.sendVerificationCode(email, code);
            return true;
        } catch (MailException e) {
            log.warn("Could not send verification email to {}: {}", email, e.getMessage());
            return false;
        }
    }

    /** Throws 400 unless the code is right. On success the code is used up. */
    public void confirm(String email, String code) {
        EmailVerification v = repo.findByEmailIgnoreCase(email).orElseThrow(EmailVerificationService::invalid);
        if (v.getExpiresAt().isBefore(LocalDateTime.now())) {
            repo.delete(v);
            throw invalid();
        }
        if (code == null || !encoder.matches(code.strip(), v.getCodeHash())) {
            v.setTries(v.getTries() + 1);
            if (v.getTries() >= MAX_TRIES) repo.delete(v); else repo.save(v);
            throw invalid();
        }
        repo.delete(v);
    }

    private static ApiException invalid() {
        return new ApiException(400, "Verification code is invalid or expired");
    }
}
