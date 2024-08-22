package com.inconcert.global.exception;

public class CommentDeleteUnauthorizedException extends RuntimeException {
    public CommentDeleteUnauthorizedException(String message) {
        super(message);
    }
}
