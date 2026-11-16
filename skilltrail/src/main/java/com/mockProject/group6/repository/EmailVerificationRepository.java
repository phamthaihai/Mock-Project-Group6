package com.mockProject.group6.repository;

import com.mockProject.group6.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {

    Optional<EmailVerification> findByEmailIgnoreCase(String email);
}
