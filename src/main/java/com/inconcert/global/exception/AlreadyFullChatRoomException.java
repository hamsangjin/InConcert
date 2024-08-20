package com.inconcert.global.exception;

public class AlreadyFullChatRoomException extends RuntimeException {
    public AlreadyFullChatRoomException(String message) {
        super(message);
    }
}
