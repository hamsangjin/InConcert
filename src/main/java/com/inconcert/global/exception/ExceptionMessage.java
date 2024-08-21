package com.inconcert.global.exception;

public enum ExceptionMessage {
    // 게시글 및 카테고리 관련 에러 메시지
    CATEGORY_NOT_FOUND("찾으려는 카테고리가 존재하지 않습니다."),
    POST_CATEGORY_COMBINATION_NOT_FOUND("해당 제목과 카테고리 조합의 게시글 카테고리를 찾지 못했습니다."),
    POST_CATEGORY_NOT_FOUND("찾으려는 게시글 카테고리가 존재하지 않습니다."),
    POST_NOT_FOUND("찾으려는 게시글이 존재하지 않습니다."),

    // 댓글 관련 에러 메시지
    COMMENT_NOT_FOUND("찾으려는 댓글이 존재하지 않습니다."),

    LIKE_NOT_FOUND("찾으려는 좋아요가 존재하지 않습니다."),

    // 유저 관련 에러 메시지
    USER_NOT_FOUND("찾으려는 유저가 존재하지 않습니다."),
    ROLE_NOT_FOUND("찾으려는 Role이 존재하지 않습니다."),

    // 이미지 관련 에러 메시지
    IMAGE_UPLOAD_BAD_REQUEST("이미지 업로드에 실패했습니다."),

    // 알림 관련 에러 메시지
    NOTIFICATION_NOT_FOUND("찾으려는 알림이 존재하지 않습니다."),
    KEYWORD_NOT_FOUND("찾으려는 키워드가 존재하지 않습니다."),

    // 신고 관련 에러 메시지
    REPORT_NOT_FOUND("찾으려는 신고가 존재하지 않습니다."),

    // 채팅 관련 에러 메시지
    CHAT_NOT_FOUND("찾으려는 채팅방이 존재하지 않습니다."),
    CHAT_NOTIFICATION_NOT_FOUND("찾으려는 채팅방 알림이 존재하지 않습니다."),
    ALREADY_IN_CHATROOM("이미 채팅방에 속해있습니다."),
    ALREADY_FULL_CHATROOM("이미 채팅방이 가득 찼습니다."),
    HOST_EXIT("호스트는 채팅방에 본인만 존재할 때 퇴장할 수 있습니다."),
    ALREADY_OUT_OF_CHATROOM("채팅방에 속해있지 않습니다."),
    EXIST_CHAT_POST_DELETE("연결된 채팅방이 있는 경우 포스트를 삭제할 수 없습니다.");




    private final String message;

    ExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}