package com.mockProject.group6.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.post-dir:uploads/posts}")
    private String postUploadDir;

    @Value("${app.upload.avatar-dir:uploads/avatars}")
    private String avatarUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path postPath = Paths.get(postUploadDir).toAbsolutePath().normalize();
        Path avatarPath = Paths.get(avatarUploadDir).toAbsolutePath().normalize();

        registry.addResourceHandler("/uploads/posts/**")
                .addResourceLocations("file:" + postPath.toString() + "/");

        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + avatarPath.toString() + "/");
    }
}