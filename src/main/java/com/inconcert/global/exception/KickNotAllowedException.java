package com.inconcert.global.exception;

public class KickNotAllowedException extends RuntimeException {
    public KickNotAllowedException(String message) {
        super(message);
    }
}
