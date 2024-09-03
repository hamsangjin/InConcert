package com.inconcert.common.exception;

import lombok.Getter;

@Getter
public enum JwtExceptionCode {
    UNKNOWN_ERROR("UNKNOWN_ERROR", "알 수 없는 에러"),
    NOT_FOUND_TOKEN("NOT_FOUND_TOKEN", "Headers에 토큰 형식의 값 찾을 수 없음"),
    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰"),
    EXPIRED_TOKEN("EXPIRED_TOKEN", "기간이 만료된 토큰"),
    UNSUPPORTED_TOKEN("UNSUPPORTED_TOKEN", "지원하지 않는 토큰");

    private String code;
    private String message;

    JwtExceptionCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
