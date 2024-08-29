package com.inconcert.common.exception;

public class AlreadyOutOfChatRoomException extends RuntimeException {
    public AlreadyOutOfChatRoomException(String message) {
        super(message);
    }
}
