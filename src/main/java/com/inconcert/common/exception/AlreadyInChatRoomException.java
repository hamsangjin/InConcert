package com.inconcert.common.exception;

public class AlreadyInChatRoomException extends RuntimeException {
    public AlreadyInChatRoomException(String message) {
        super(message);
    }
}
