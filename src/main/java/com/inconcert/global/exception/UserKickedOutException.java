package com.inconcert.global.exception;

public class UserKickedOutException extends RuntimeException {
    public UserKickedOutException(String message) {
        super(message);
    }
}
