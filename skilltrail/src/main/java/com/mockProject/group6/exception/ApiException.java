package com.mockProject.group6.exception;

import java.util.Map;

public class ApiException extends RuntimeException {

    private final int status;
    private final Map<String, String> errors;

    public ApiException(int status, String message) {
        this(status, message, Map.of());
    }

    public ApiException(int status, String message, Map<String, String> errors) {
        super(message);
        this.status = status;
        this.errors = errors;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
