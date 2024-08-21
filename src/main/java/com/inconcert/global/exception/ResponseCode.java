package com.inconcert.global.exception;

public interface ResponseCode {
    String SUCCESS = "SU";
    String VALIDATION_FAIL = "VF";
    String DUPLICATE_ID = "DI";
    String DUPLICATE_EMAIL = "DE";
    String DUPLICATE_NICKNAME = "DN";
    String PASSWORD_NOT_MATCH = "PNM";

    String DUPLICATE_PHONE_NUMBER = "DP";
    String CERTIFICATION_FAIL = "CF";
    String MAIL_FAIL = "MF";
    String DATABASE_ERROR = "DBE";
}