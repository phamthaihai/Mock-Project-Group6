package com.mockProject.group6.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.mockProject.group6.dto.ForgotPasswordDTO;
import com.mockProject.group6.dto.LoginDTO;
import com.mockProject.group6.dto.ProfileDTO;
import com.mockProject.group6.dto.RegisterDTO;
import com.mockProject.group6.dto.ResetPasswordDTO;
import com.mockProject.group6.dto.UserDTO;
import com.mockProject.group6.entity.Role;
import com.mockProject.group6.entity.User;
import com.mockProject.group6.entity.UserStatus;
import com.mockProject.group6.repository.RoleRepository;
import com.mockProject.group6.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final SecureRandom RESET_TOKEN_RANDOM = new SecureRandom();
    private static final int RESET_TOKEN_BYTES = 32;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User login(LoginDTO loginDTO) {
        if (loginDTO == null || loginDTO.getEmail() == null || loginDTO.getPassword() == null) {
            return null;
        }

        String email = loginDTO.getEmail().trim().toLowerCase();
        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(email);
        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        String rawPassword = loginDTO.getPassword();
        String stored = user.getPasswordHash();
        if (stored == null) {
            return null;
        }

        stored = stored.trim();
        if (stored.length() >= 2 && stored.startsWith("'") && stored.endsWith("'")) {
            stored = stored.substring(1, stored.length() - 1).trim();
        }

        boolean passwordOk = false;
        try {
            passwordOk = encoder.matches(rawPassword, stored);
        } catch (Exception ignored) {
        }

        if (!passwordOk) {
            passwordOk = rawPassword.equals(stored);
        }

        if (!passwordOk || user.getStatus() != UserStatus.ACTIVE) {
            return null;
        }

        return user;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public String encodePassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matchesPassword(String rawPassword, String passwordHash) {
        if (passwordHash == null || rawPassword == null) {
            return false;
        }

        String stored = passwordHash.trim();
        if (stored.length() >= 2 && stored.startsWith("'") && stored.endsWith("'")) {
            stored = stored.substring(1, stored.length() - 1).trim();
        }

        try {
            if (encoder.matches(rawPassword, stored)) {
                return true;
            }
        } catch (Exception ignored) {
        }

        return rawPassword.equals(stored);
    }

    


    @Transactional
    public Optional<String> createPasswordResetToken(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(normalizedEmail);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        byte[] tokenBytes = new byte[RESET_TOKEN_BYTES];
        RESET_TOKEN_RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        user.setPasswordResetTokenHash(hashPasswordResetToken(token));
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        return Optional.of(token);
    }

    public boolean isPasswordResetTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        return userRepository.findByPasswordResetTokenHash(hashPasswordResetToken(token))
                .filter(user -> user.getPasswordResetTokenExpiresAt() != null
                        && user.getPasswordResetTokenExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("This reset link is invalid or has expired");
        }
        if (newPassword == null || newPassword.length() < 7 || newPassword.length() > 100) {
            throw new IllegalArgumentException("Password must be from 7 to 100 characters");
        }

        User user = userRepository.findByPasswordResetTokenHash(hashPasswordResetToken(token))
                .filter(candidate -> candidate.getPasswordResetTokenExpiresAt() != null
                        && candidate.getPasswordResetTokenExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException("This reset link is invalid or has expired"));

        user.setPasswordHash(encodePassword(newPassword));
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetTokenExpiresAt(null);
        userRepository.save(user);
    }

    private String hashPasswordResetToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    @Transactional
    public User updateProfile(Integer userId, ProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fullName = dto.getFullName() == null ? "" : dto.getFullName().trim();
        String email = dto.getEmail() == null ? "" : dto.getEmail().trim().toLowerCase();
        String phone = dto.getPhone() == null ? "" : dto.getPhone().trim();

        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByFullNameIgnoreCaseAndIdNot(fullName, userId)) {
            throw new RuntimeException("Full name already exists");
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Integer userId, String oldPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!matchesPassword(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("Wrong current password");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Confirm password does not match");
        }

        if (matchesPassword(newPassword, user.getPasswordHash())) {
            throw new RuntimeException("New password must be different from current password");
        }

        user.setPasswordHash(encodePassword(newPassword));
        userRepository.save(user);
    }

     @Autowired
    private FileStorageService fileStorageService;

    @Transactional
    public User updateProfile(Integer userId, ProfileDTO dto, org.springframework.web.multipart.MultipartFile avatarFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fullName = dto.getFullName() == null ? "" : dto.getFullName().trim();
        String email = dto.getEmail() == null ? "" : dto.getEmail().trim().toLowerCase();
        String phone = dto.getPhone() == null ? "" : dto.getPhone().trim();

        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByFullNameIgnoreCaseAndIdNot(fullName, userId)) {
            throw new RuntimeException("Full name already exists");
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarPath = fileStorageService.saveAvatar(avatarFile);
            user.setAvatar(avatarPath);
        }

        return userRepository.save(user);
    }

    @Transactional
    public User processOAuth2User(String email, String fullName, String avatarUrl) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email not found from OAuth2 provider");
        }
        
        String normalizedEmail = email.trim().toLowerCase();
        
        // Sử dụng findByEmailWithRole để load kèm Role nếu có, tránh LazyInitializationException
        Optional<User> userOptional = userRepository.findByEmailWithRole(normalizedEmail);
        
        User user;
        if (userOptional.isPresent()) {
            // 1. Đã có account -> Kiểm tra và cập nhật thông tin nếu thiếu
            user = userOptional.get();
            if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
                user.setFullName(fullName);
            }
            if (user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
                user.setAvatar(avatarUrl);
            }
            if (user.getStatus() == null) {
                user.setStatus(UserStatus.ACTIVE);
            }
            user = userRepository.save(user);
        } else {
            // 2. Lần đầu tiên login/register bằng OAuth2 -> Tạo mới account
            user = new User();
            user.setEmail(normalizedEmail);
            user.setFullName(fullName != null ? fullName : "OAuth2 User");
            user.setAvatar(avatarUrl);
            user.setStatus(UserStatus.ACTIVE);
            user.setEmailVerified(true); // Tài khoản Google/Facebook mặc định đã xác thực email
            // Đặt password ngẫu nhiên được mã hóa vì user này không dùng mật khẩu thường
            user.setPasswordHash(encodePassword(java.util.UUID.randomUUID().toString()));
            
            // Tìm và gán role mặc định cho user mới theo dữ liệu thực tế của project.
            try {
                Role defaultRole = roleRepository.findByName("USER")
                        .or(() -> roleRepository.findByName("MEMBER"))
                        .or(() -> roleRepository.findByName("ADMIN"))
                        .orElse(null);
                if (defaultRole != null) {
                    user.setRole(defaultRole);
                }
            } catch (Exception ignored) {
            }

            user = userRepository.save(user);
        }
        return user;
    }

}