package com.mockProject.group6.security;

import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OAuth2ClientConfiguration {

    public ClientRegistrationRepository clientRegistrationRepository(Environment environment) {
        List<ClientRegistration> registrations = new ArrayList<>();

        String googleClientId = environment.getProperty("app.oauth.google.client-id");
        String googleClientSecret = environment.getProperty("app.oauth.google.client-secret");
        if (StringUtils.hasText(googleClientId) && StringUtils.hasText(googleClientSecret)) {
            registrations.add(ClientRegistration.withRegistrationId("google")
                    .clientId(googleClientId)
                    .clientSecret(googleClientSecret)
                    .clientName("Google")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                    .userNameAttributeName("sub")
                    .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                    .build());
        }

        String facebookClientId = environment.getProperty("app.oauth.facebook.client-id");
        String facebookClientSecret = environment.getProperty("app.oauth.facebook.client-secret");
        if (StringUtils.hasText(facebookClientId) && StringUtils.hasText(facebookClientSecret)) {
            registrations.add(ClientRegistration.withRegistrationId("facebook")
                    .clientId(facebookClientId)
                    .clientSecret(facebookClientSecret)
                    .clientName("Facebook")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("public_profile", "email")
                    .authorizationUri("https://www.facebook.com/v18.0/dialog/oauth")
                    .tokenUri("https://graph.facebook.com/v18.0/oauth/access_token")
                    .userInfoUri("https://graph.facebook.com/me?fields=id,name,email,picture")
                    .userNameAttributeName("id")
                    .build());
        }

        return new InMemoryClientRegistrationRepository(registrations);
    }
}
