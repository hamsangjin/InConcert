package com.inconcert.common.exception;

public class PostCategoryNotFoundException extends RuntimeException {
    public PostCategoryNotFoundException(String message) {
        super(message);
    }
}