package com.mockProject.group6.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class UploadController {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @PostMapping("/upload/image")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("error", "File is empty");
            return ResponseEntity.badRequest().body(response);
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            response.put("error", "Only image files are allowed");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Create upload directory if not exists
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, file.getBytes());

            // Return URL to access the file
            String contextPath = request.getContextPath();
            String fileUrl = contextPath + "/uploads/" + filename;

            response.put("url", fileUrl);
            response.put("filename", filename);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/uploads/**")
    @ResponseBody
    public ResponseEntity<byte[]> serveFile(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String filename = requestUri.substring(requestUri.lastIndexOf("/uploads/") + 9);

        try {
            Path filePath = Paths.get(uploadDir, filename);
            if (Files.exists(filePath)) {
                byte[] fileContent = Files.readAllBytes(filePath);
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .header("Content-Type", contentType)
                        .body(fileContent);
            }
        } catch (IOException e) {
            // File not found
        }
        return ResponseEntity.notFound().build();
    }
}
