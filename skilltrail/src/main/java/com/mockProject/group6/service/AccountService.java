package com.mockProject.group6.service;

import com.mockProject.group6.exception.ApiException;
import com.mockProject.group6.security.AuthUser;
import com.mockProject.group6.security.JwtService;
import com.mockProject.group6.support.Db;
import com.mockProject.group6.support.In;
import com.mockProject.group6.support.Json;
import com.mockProject.group6.support.Rows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Login, Google sign-in, forgot / reset password and the "my account" calls.
 * Not @Transactional on purpose: failed-login and wrong-code counters must be saved even though an error is thrown afterwards.
 */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    private static final String NO_PASSWORD = "!"; // PasswordHash of accounts that only use Google sign-in

    private final JdbcTemplate jdbc;
    private final ActivityService act;
    private final JwtService jwt;
    private final PasswordEncoder encoder;
    private final MailService mail;
    private final String googleClientId;
    private final SecureRandom random = new SecureRandom();

    public AccountService(JdbcTemplate jdbc, ActivityService act, JwtService jwt, PasswordEncoder encoder, MailService mail,
                          @Value("${skilltrail.google.client-id:}") String googleClientId) {
        this.jdbc = jdbc;
        this.act = act;
        this.jwt = jwt;
        this.encoder = encoder;
        this.mail = mail;
        this.googleClientId = googleClientId;
    }

    private static void need(boolean ok, String message) {
        if (!ok) throw new ApiException(400, message);
    }

    private static final String USER_COLUMNS = "SELECT UserId, FullName, Email, PasswordHash, Role, IsAuthor, Locked, Verified, Avatar, GoogleLinked FROM Users ";

    private Map<String, Object> userByEmail(String email) {
        List<Map<String, Object>> l = jdbc.queryForList(USER_COLUMNS + "WHERE Email = ?", email);
        return l.isEmpty() ? null : l.get(0);
    }

    private Map<String, Object> userById(int id) {
        List<Map<String, Object>> l = jdbc.queryForList(USER_COLUMNS + "WHERE UserId = ?", id);
        if (l.isEmpty()) throw new ApiException(404, "User not found");
        return l.get(0);
    }

    private static boolean hasPassword(Map<String, Object> u) {
        String h = Rows.s(u, "PasswordHash");
        return h != null && !h.isEmpty() && !NO_PASSWORD.equals(h);
    }

    private static boolean googleLinked(Map<String, Object> u) {
        return Boolean.TRUE.equals(u.get("GoogleLinked"));
    }

    /** GoogleLinked is NULL for "never used Google", 1 linked, 0 unbound. */
    private static boolean googleUnbound(Map<String, Object> u) {
        return Boolean.FALSE.equals(u.get("GoogleLinked"));
    }

    /** The user JSON used by GET/PUT /users/me. */
    public static Map<String, Object> pubUser(Map<String, Object> u) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("userId", Rows.i(u, "UserId"));
        o.put("fullName", Rows.s(u, "FullName"));
        o.put("email", Rows.s(u, "Email"));
        o.put("role", Rows.s(u, "Role"));
        o.put("locked", Rows.b(u, "Locked"));
        o.put("author", Rows.b(u, "IsAuthor"));
        o.put("avatar", Rows.sn(u, "Avatar"));
        o.put("hasPassword", hasPassword(u));
        o.put("googleLinked", googleLinked(u));
        return o;
    }

    private Map<String, Object> session(Map<String, Object> u, Boolean created) {
        int id = Rows.i(u, "UserId");
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("userId", id);
        user.put("fullName", Rows.s(u, "FullName"));
        user.put("email", Rows.s(u, "Email"));
        user.put("role", Rows.s(u, "Role"));
        user.put("avatar", Rows.sn(u, "Avatar"));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("token", jwt.create(id, Rows.s(u, "Role")));
        if (created != null) out.put("created", created);
        out.put("user", user);
        return out;
    }

    /* ------------------------------------------------------------------ login */

    /** POST /auth/login */
    public Map<String, Object> login(Map<String, Object> b) {
        String email = In.trim(b, "email");
        String password = In.str(b, "password") == null ? "" : In.str(b, "password");
        Map<String, Object> u = email.isEmpty() ? null : userByEmail(email);
        if (u == null || !hasPassword(u) || !encoder.matches(password, Rows.s(u, "PasswordHash"))) {
            act.account("LOGIN_FAILED", null, email, "Wrong email or password", "");
            throw new ApiException(401, "Invalid email or password");
        }
        int id = Rows.i(u, "UserId");
        String name = Rows.s(u, "FullName");
        if (Rows.b(u, "Locked")) {
            act.account("LOGIN_BLOCKED", id, Rows.s(u, "Email"), "Account is locked", name);
            throw new ApiException(403, "This account has been locked. Contact an admin.");
        }
        if (!Rows.b(u, "Verified")) throw new ApiException(403, "Please verify your email first");
        act.account("LOGIN", id, Rows.s(u, "Email"), "Email and password", name);
        return session(u, null);
    }

    /* ------------------------------------------------------------------ Google */

    private Map<String, Object> tokenInfo(String credential) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token="
                    + URLEncoder.encode(credential, StandardCharsets.UTF_8))).timeout(Duration.ofSeconds(8)).GET().build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) throw new ApiException(401, "Invalid Google sign-in token");
            return Json.parseMap(res.body());
        } catch (ApiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(401, "Invalid Google sign-in token");
        } catch (Exception e) {
            log.warn("Google token check failed: {}", e.toString());
            throw new ApiException(401, "Invalid Google sign-in token");
        }
    }

    /** POST /auth/google {credential}: the token is verified by Google itself (tokeninfo), then audience and expiry are checked. */
    public Map<String, Object> google(Map<String, Object> b) {
        String credential = In.trim(b, "credential");
        if (credential.isEmpty()) throw new ApiException(401, "Invalid Google sign-in token");
        Map<String, Object> g = tokenInfo(credential);
        String email = g.get("email") == null ? "" : g.get("email").toString().trim();
        need(!email.isEmpty() && "true".equalsIgnoreCase(String.valueOf(g.get("email_verified"))), "Your Google email address is not verified");
        if (!googleClientId.isBlank() && !googleClientId.equals(String.valueOf(g.get("aud")))) {
            throw new ApiException(401, "This Google token was issued for a different app");
        }
        try {
            if (Long.parseLong(String.valueOf(g.get("exp"))) * 1000 < System.currentTimeMillis()) {
                throw new ApiException(401, "Google sign-in expired. Please try again.");
            }
        } catch (NumberFormatException e) {
            throw new ApiException(401, "Invalid Google sign-in token");
        }
        String picture = g.get("picture") == null ? "" : g.get("picture").toString();
        String name = g.get("name") == null || g.get("name").toString().isBlank() ? email.split("@")[0] : g.get("name").toString().trim();
        Map<String, Object> u = userByEmail(email);
        boolean created = false;
        if (u != null && Rows.b(u, "Locked")) throw new ApiException(403, "This account has been locked. Contact an admin.");
        if (u != null && googleUnbound(u)) throw new ApiException(403, "Google sign-in is unbound from this account. Log in with your email and password.");
        if (u == null) {
            int id = Db.insert(jdbc, "INSERT INTO Users (FullName, Email, PasswordHash, Role, Verified, Avatar, GoogleLinked) VALUES (?, ?, ?, 'STUDENT', 1, ?, 1)",
                    In.cut(name, 100), In.cut(email, 150), NO_PASSWORD, picture);
            u = userById(id);
            created = true;
        } else {
            jdbc.update("UPDATE Users SET GoogleLinked = 1, Verified = 1, Avatar = CASE WHEN Avatar IS NULL OR Avatar = '' THEN ? ELSE Avatar END WHERE UserId = ?",
                    picture, Rows.i(u, "UserId"));
            u = userById(Rows.i(u, "UserId"));
        }
        act.account(created ? "REGISTERED" : "LOGIN", Rows.i(u, "UserId"), Rows.s(u, "Email"), "Google sign-in", Rows.s(u, "FullName"));
        return session(u, created);
    }

    /* ------------------------------------------------------------------ forgot / reset password */

    private String sixDigits() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    /** POST /auth/forgot-password {email}: emails a 6-digit code (valid 10 minutes). The answer never reveals whether the email exists. */
    public Map<String, Object> forgotPassword(Map<String, Object> b) {
        String email = In.trim(b, "email").toLowerCase();
        need(email.matches("^\\S+@\\S+\\.\\S+$"), "Enter a valid email");
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("message", "If that email is registered, a reset code has been sent.");
        Map<String, Object> u = userByEmail(email);
        if (u == null || Rows.b(u, "Locked")) return msg;
        List<Map<String, Object>> old = jdbc.queryForList("SELECT SentAt FROM PasswordResets WHERE Email = ?", email);
        if (!old.isEmpty() && old.get(0).get("SentAt") instanceof Timestamp
                && ((Timestamp) old.get(0).get("SentAt")).toLocalDateTime().isAfter(LocalDateTime.now().minusSeconds(60))) {
            return msg; // a code was sent less than a minute ago
        }
        String code = sixDigits();
        jdbc.update("DELETE FROM PasswordResets WHERE Email = ?", email);
        jdbc.update("INSERT INTO PasswordResets (Email, CodeHash, ExpiresAt, Tries, SentAt) VALUES (?, ?, ?, 0, ?)", email, encoder.encode(code),
                Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)), Timestamp.valueOf(LocalDateTime.now()));
        act.account("RESET_REQUESTED", Rows.i(u, "UserId"), Rows.s(u, "Email"), "Forgot password", Rows.s(u, "FullName"));
        try {
            mail.sendResetCode(Rows.s(u, "Email"), code);
        } catch (MailException e) {
            log.warn("Could not send reset email to {}: {}", email, e.getMessage());
        }
        return msg;
    }

    /** POST /auth/reset-password {email, code, newPassword} */
    public void resetPassword(Map<String, Object> b) {
        String email = In.trim(b, "email").toLowerCase();
        String code = In.trim(b, "code");
        String pw = In.str(b, "newPassword") == null ? "" : In.str(b, "newPassword");
        need(pw.length() >= 6, "Password must be at least 6 characters");
        need(pw.length() <= 64, "Password must be at most 64 characters");
        String bad = "Reset code is invalid or expired";
        List<Map<String, Object>> rs = jdbc.queryForList("SELECT CodeHash, ExpiresAt, Tries FROM PasswordResets WHERE Email = ?", email);
        Map<String, Object> u = userByEmail(email);
        if (rs.isEmpty() || u == null) throw new ApiException(400, bad);
        Map<String, Object> r = rs.get(0);
        Timestamp exp = (Timestamp) r.get("ExpiresAt");
        if (exp.toLocalDateTime().isBefore(LocalDateTime.now())) {
            jdbc.update("DELETE FROM PasswordResets WHERE Email = ?", email);
            throw new ApiException(400, bad);
        }
        if (!encoder.matches(code, Rows.s(r, "CodeHash"))) {
            if (Rows.i(r, "Tries") + 1 >= 5) jdbc.update("DELETE FROM PasswordResets WHERE Email = ?", email);
            else jdbc.update("UPDATE PasswordResets SET Tries = Tries + 1 WHERE Email = ?", email);
            throw new ApiException(400, bad);
        }
        jdbc.update("UPDATE Users SET PasswordHash = ? WHERE UserId = ?", encoder.encode(pw), Rows.i(u, "UserId"));
        jdbc.update("DELETE FROM PasswordResets WHERE Email = ?", email);
        act.account("PASSWORD_RESET", Rows.i(u, "UserId"), Rows.s(u, "Email"), "Forgot password", Rows.s(u, "FullName"));
    }

    /* ------------------------------------------------------------------ my account */

    public Map<String, Object> me(AuthUser me) {
        return pubUser(userById(me.userId()));
    }

    /** PUT /users/me {fullName, avatar?} */
    public Map<String, Object> updateMe(AuthUser me, Map<String, Object> b) {
        String name = In.trim(b, "fullName");
        need(!name.isEmpty(), "Full name is required");
        if (b.get("avatar") instanceof String) {
            jdbc.update("UPDATE Users SET FullName = ?, Avatar = ? WHERE UserId = ?", In.cut(name, 100), b.get("avatar"), me.userId());
        } else {
            jdbc.update("UPDATE Users SET FullName = ? WHERE UserId = ?", In.cut(name, 100), me.userId());
        }
        return pubUser(userById(me.userId()));
    }

    /** PUT /users/me/password {currentPassword, newPassword}; accounts without a password may set one without the current one. */
    public void changePassword(AuthUser me, Map<String, Object> b) {
        Map<String, Object> u = userById(me.userId());
        String current = In.str(b, "currentPassword") == null ? "" : In.str(b, "currentPassword");
        if (hasPassword(u) && !encoder.matches(current, Rows.s(u, "PasswordHash"))) throw new ApiException(400, "Current password is incorrect");
        String pw = In.str(b, "newPassword") == null ? "" : In.str(b, "newPassword");
        need(pw.length() >= 6, "Password must be at least 6 characters");
        need(pw.length() <= 64, "Password must be at most 64 characters");
        jdbc.update("UPDATE Users SET PasswordHash = ? WHERE UserId = ?", encoder.encode(pw), me.userId());
        act.account("PASSWORD_CHANGED", me.userId(), me.email(), "Changed by the user", me.fullName());
    }

    /** POST /users/me/google/unbind {newPassword?, force?} */
    public void unbindGoogle(AuthUser me, Map<String, Object> b) {
        Map<String, Object> u = userById(me.userId());
        need(googleLinked(u), "No Google account is linked");
        if (!hasPassword(u)) {
            String pw = In.str(b, "newPassword");
            if (pw != null && !pw.isEmpty()) {
                need(pw.length() >= 6, "Password must be at least 6 characters");
                need(pw.length() <= 64, "Password must be at most 64 characters");
                jdbc.update("UPDATE Users SET PasswordHash = ? WHERE UserId = ?", encoder.encode(pw), me.userId());
            } else {
                need(Boolean.TRUE.equals(b.get("force")), "Set a password (min 6 characters) before unbinding Google");
            }
        }
        jdbc.update("UPDATE Users SET GoogleLinked = 0, Avatar = CASE WHEN Avatar LIKE 'http%' THEN '' ELSE Avatar END WHERE UserId = ?", me.userId());
    }

    /* ------------------------------------------------------------------ notifications */

    /** GET /notifications/me (latest 20) */
    public List<Map<String, Object>> notifications(AuthUser me) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT TOP 20 NotificationId, Message, Link, IsRead, CreatedAt FROM Notifications WHERE UserId = ? ORDER BY NotificationId DESC", me.userId());
        List<Map<String, Object>> out = new java.util.ArrayList<>();
        for (Map<String, Object> n : rows) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("notificationId", Rows.i(n, "NotificationId"));
            o.put("message", Rows.s(n, "Message"));
            o.put("link", Rows.s(n, "Link"));
            o.put("read", Rows.b(n, "IsRead"));
            o.put("createdAt", Rows.ts(n, "CreatedAt"));
            out.add(o);
        }
        return out;
    }

    /** POST /notifications/read */
    public void markNotificationsRead(AuthUser me) {
        jdbc.update("UPDATE Notifications SET IsRead = 1 WHERE UserId = ? AND IsRead = 0", me.userId());
    }
}
