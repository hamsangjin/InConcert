package com.inconcert.global.exception;

public class CommentEditUnauthorizedException extends RuntimeException {
    public CommentEditUnauthorizedException(String message) {
        super(message);
    }
}
