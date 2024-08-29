package com.inconcert.common.exception;

public class LikeNotFoundException extends RuntimeException {
    public LikeNotFoundException(String message) {
        super(message);
    }
}