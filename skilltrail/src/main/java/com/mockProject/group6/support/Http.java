package com.mockProject.group6.support;

import org.springframework.http.ResponseEntity;

/** 200 with a JSON body, or 204 with no body (what the front-end api() expects for "returns nothing"). */
public final class Http {

    private Http() {
    }

    public static ResponseEntity<Object> ok(Object body) {
        return ResponseEntity.ok(body);
    }

    public static ResponseEntity<Object> none() {
        return ResponseEntity.noContent().build();
    }
}
