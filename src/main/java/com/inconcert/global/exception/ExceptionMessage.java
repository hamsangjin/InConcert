package com.inconcert.global.exception;

public enum ExceptionMessage {
    CATEGORY_NOT_FOUND("찾으려는 Category가 존재하지 않습니다."),
    POST_CATEGORY_COMBINATION_NOT_FOUND("해당 제목과 카테고리 조합의 PostCategory를 찾지 못했습니다."),
    POST_CATEGORY_NOT_FOUND("찾으려는 PostCategory가 존재하지 않습니다."),
    POST_NOT_FOUND("찾으려는 Post가 존재하지 않습니다."),
    USER_NOT_FOUND("찾으려는 Category가 존재하지 않습니다."),
    IMAGE_UPLOAD_BAD_REQUEST("이미지 업로드에 실패했습니다."),
    NOTIFICATION_NOT_FOUND("알림이 존재하지 않습니다.");


    private final String message;

    ExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}