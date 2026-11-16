package com.mockProject.group6.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mockProject.group6.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailWithRole(@org.springframework.data.repository.query.Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsByFullNameIgnoreCase(String fullName);

    boolean existsByFullNameIgnoreCaseAndIdNot(String fullName, Integer id);

    boolean existsByEmailAndIdNot(String email, Integer id);

    Optional<User> findByVerifyToken(String verifyToken);

    Optional<User> findByPasswordResetTokenHash(String passwordResetTokenHash);

    List<User> findByRole_Name(String roleName);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(String email, String fullName);
}