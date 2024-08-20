package com.inconcert.global.exception;

public class AlreadyOutOfChatRoomException extends RuntimeException {
    public AlreadyOutOfChatRoomException(String message) {
        super(message);
    }
}
