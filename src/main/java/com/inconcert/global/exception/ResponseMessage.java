package com.inconcert.global.exception;

public interface ResponseMessage {
    String SUCCESS = "Success";
    String VALIDATION_FAIL = "Validation failed.";
    String DUPLICATE_ID = "Duplicate id.";
    String DUPLICATE_EMAIL = "Duplicate email.";
    String DUPLICATE_NICKNAME = "Duplicate nickname.";
    String PASSWORD_NOT_MATCH = "Password not match.";

    String DUPLICATE_PHONE_NUMBER = "Duplicate phoneNumber";
    String CERTIFICATION_FAIL = "Certification failed.";
    String MAIL_FAIL = "Mail send failed.";
    String DATABASE_ERROR = "Database error.";
}
