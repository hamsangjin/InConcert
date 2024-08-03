package com.inconcert.global.exception;

public interface ResponseMessage {
    String SUCCESS = "Success";
    String VALIDATION_FAIL = "Validation failed.";
    String DUPLICATE_ID = "Duplicate id.";
    String DUPLICATE_EMAIL = "Duplicate email.";
    String SIGN_IN_FAIL = "Login information mismatch.";
    String CERTIFICATION_FAIL = "Certification failed.";
    String MAIL_FAIL = "Mail send failed.";
    String DATABASE_ERROR = "Database error.";
}
