package com.mockProject.group6.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            OAuth2LoginSuccessHandler oauthSuccessHandler,
            ObjectProvider<ClientRegistrationRepository> registrationRepository
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        // 1. Cho phép truy cập công khai các trang chủ, auth, tài nguyên tĩnh (css, js, images)
                        .requestMatchers(
                                "/", 
                                "/login", 
                                "/register", 
                                "/forgot-password", 
                                "/reset-password",
                                "/api/auth/**", 
                                "/oauth2/**", 
                                "/login/oauth2/**", 
                                "/css/**", 
                                "/js/**", 
                                "/images/**"
                        ).permitAll()
                        
                        // 2. Các API công khai theo định nghĩa cũ của bạn
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categories",
                                "/api/courses",
                                "/api/courses/*",
                                "/api/courses/*/reviews",
                                "/api/courses/*/progress",
                                "/api/lessons/*"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/support").permitAll()
                        
                        // 3. Tất cả các request còn lại đều phải đăng nhập
                        .anyRequest().authenticated()
                )
                .exceptionHandling(errors -> errors
                        // Tự động redirect về trang /login nếu người dùng chưa đăng nhập mà truy cập trang bảo mật
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                );

        // Cấu hình OAuth2 Login nếu có ClientRegistrationRepository (Google/Facebook)
        if (registrationRepository.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                    .loginPage("/login")
                    .successHandler(oauthSuccessHandler)
            );
        }

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}