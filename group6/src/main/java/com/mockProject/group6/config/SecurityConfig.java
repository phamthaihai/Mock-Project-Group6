
package com.mockProject.group6.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(
                                "/", "/login", "/register", "/verify", "/logout",
                                "/user-profile", "/change-password",
                                "/upload/image", "/uploads/**",
                                "/forgot-password", "/reset-password",
                                "/api/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/courses",
                                "/courses/*"
                        ).permitAll()

                        .requestMatchers(
                                "/my-learning",
                                "/my-courses/**",
                                "/cart",
                                "/cart/**",
                                "/checkout",
                                "/courses/*/enroll",
                                "/courses/*/learn",
                                "/courses/*/lessons/*",
                                "/courses/*/lessons/*/progress"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categories",
                                "/api/courses",
                                "/api/courses/*",
                                "/api/courses/*/reviews",
                                "/api/courses/*/progress",
                                "/api/lessons/*"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/support"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(
                                new LoginUrlAuthenticationEntryPoint("/login")
                        )
                );

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
