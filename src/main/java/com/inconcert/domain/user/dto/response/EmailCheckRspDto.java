package com.inconcert.domain.user.dto.response;

import com.inconcert.common.dto.ResponseDto;
import com.inconcert.common.exception.ResponseCode;
import com.inconcert.common.exception.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class EmailCheckRspDto extends ResponseDto {
    public EmailCheckRspDto() {
        super();
    }

    public static ResponseEntity<EmailCheckRspDto> success() {
        EmailCheckRspDto responseBody = new EmailCheckRspDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> duplicateEmail() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.DUPLICATE_EMAIL, ResponseMessage.DUPLICATE_EMAIL);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}
