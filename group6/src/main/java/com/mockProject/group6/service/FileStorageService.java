package com.mockProject.group6.service;


import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "gif", "webp");

    public String saveAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = getExtension(originalName).toLowerCase();

        if (!ALLOWED_EXT.contains(ext)) {
            throw new RuntimeException("Only PNG, JPG, JPEG, GIF, WEBP are allowed");
        }

        try {
            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "avatars");
            Files.createDirectories(uploadDir);

            String fileName = UUID.randomUUID() + "." + ext;
            Path target = uploadDir.resolve(fileName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/avatars/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Cannot save avatar", e);
        }
    }

    private String getExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(idx + 1) : "";
    }
}