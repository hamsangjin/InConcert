package com.inconcert.common.exception;

public class AlreadyFullChatRoomException extends RuntimeException {
    public AlreadyFullChatRoomException(String message) {
        super(message);
    }
}
