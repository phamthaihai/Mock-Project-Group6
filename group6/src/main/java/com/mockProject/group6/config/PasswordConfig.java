package com.mockProject.group6.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class PasswordConfig {
    // Intentionally left empty: the password encoder bean is defined centrally in SecurityConfig.
    // Keeping both definitions causes Spring bean name collisions at startup.
}