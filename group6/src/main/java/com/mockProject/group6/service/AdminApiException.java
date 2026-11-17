package com.mockProject.group6.service;

public class AdminApiException extends RuntimeException {

    private final int status;

    public AdminApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
