package com.inconcert.global.exception;

public class PostCategoryNotFoundException extends RuntimeException {
    public PostCategoryNotFoundException(String message) {
        super(message);
    }
}