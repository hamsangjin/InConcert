package com.inconcert.common.exception;

public class UserKickedOutException extends RuntimeException {
    public UserKickedOutException(String message) {
        super(message);
    }
}
