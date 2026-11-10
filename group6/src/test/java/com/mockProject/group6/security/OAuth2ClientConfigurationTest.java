package com.mockProject.group6.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OAuth2ClientConfigurationTest {

    @Test
    void createsOnlyProvidersWithBothCredentialsConfigured() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("app.oauth.google.client-id", "google-client")
                .withProperty("app.oauth.google.client-secret", "google-secret")
                .withProperty("app.oauth.facebook.client-id", "")
                .withProperty("app.oauth.facebook.client-secret", "");

        ClientRegistrationRepository repository =
                new OAuth2ClientConfiguration().clientRegistrationRepository(environment);

        assertNotNull(repository.findByRegistrationId("google"));
        assertNull(repository.findByRegistrationId("facebook"));
    }
}
