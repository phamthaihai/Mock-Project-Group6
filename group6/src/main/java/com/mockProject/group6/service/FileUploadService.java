package com.mockProject.group6.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${app.upload.post-dir:uploads/posts}")
    private String uploadDir;

    public String uploadPostImage(MultipartFile file) {
        validate(file);

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
            String ext = "";

            int dot = originalName.lastIndexOf(".");
            if (dot >= 0) {
                ext = originalName.substring(dot);
            }

            String fileName = UUID.randomUUID() + ext;
            Path target = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/posts/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Cannot upload image");
        }
    }

    private void validate(MultipartFile file) {
        long max = 10 * 1024 * 1024L;
        if (file.getSize() > max) {
            throw new RuntimeException("Image must be smaller than 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }
    }
}
