package com.mockProject.group6.security;

import com.mockProject.group6.support.Json;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/** HS256 JSON Web Tokens without an extra library. */
@Component
public class JwtService {

    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DEC = Base64.getUrlDecoder();

    private final byte[] key;
    private final long ttlSeconds;

    public JwtService(@Value("${skilltrail.jwt.secret:skilltrail-dev-secret-change-me-before-deploying}") String secret,
                      @Value("${skilltrail.jwt.days:7}") long days) {
        this.key = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = days * 86400L;
    }

    public String create(int userId, String role) {
        long now = System.currentTimeMillis() / 1000;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(userId));
        payload.put("role", role);
        payload.put("iat", now);
        payload.put("exp", now + ttlSeconds);
        String head = ENC.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String body = ENC.encodeToString(Json.write(payload).getBytes(StandardCharsets.UTF_8));
        return head + "." + body + "." + sign(head + "." + body);
    }

    /** Returns the user id when the token is valid and not expired, otherwise null. */
    public Integer verify(String token) {
        try {
            String[] p = token.split("\\.");
            if (p.length != 3) return null;
            byte[] expected = sign(p[0] + "." + p[1]).getBytes(StandardCharsets.UTF_8);
            if (!MessageDigest.isEqual(expected, p[2].getBytes(StandardCharsets.UTF_8))) return null;
            Map<String, Object> payload = Json.parseMap(new String(DEC.decode(p[1]), StandardCharsets.UTF_8));
            Object exp = payload.get("exp");
            if (!(exp instanceof Number) || ((Number) exp).longValue() < System.currentTimeMillis() / 1000) return null;
            return Integer.valueOf(String.valueOf(payload.get("sub")));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return ENC.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
