package com.inconcert.common.exception;

public class KickNotAllowedException extends RuntimeException {
    public KickNotAllowedException(String message) {
        super(message);
    }
}
