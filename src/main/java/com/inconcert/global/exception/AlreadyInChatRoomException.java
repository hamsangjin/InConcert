package com.inconcert.global.exception;

public class AlreadyInChatRoomException extends RuntimeException {
    public AlreadyInChatRoomException(String message) {
        super(message);
    }
}
