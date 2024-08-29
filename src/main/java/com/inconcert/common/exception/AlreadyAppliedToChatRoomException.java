package com.inconcert.common.exception;

public class AlreadyAppliedToChatRoomException extends RuntimeException {
    public AlreadyAppliedToChatRoomException(String message) {
        super(message);
    }
}