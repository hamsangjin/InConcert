package com.inconcert.domain.user.dto.response;

import com.inconcert.global.dto.ResponseDto;
import com.inconcert.global.exception.ResponseCode;
import com.inconcert.global.exception.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PhoneNumberCheckRspDto extends ResponseDto {
    public PhoneNumberCheckRspDto() {
        super();
    }

    public static ResponseEntity<PhoneNumberCheckRspDto> success() {
        PhoneNumberCheckRspDto responseBody = new PhoneNumberCheckRspDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> duplicatePhoneNumber() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.DUPLICATE_PHONE_NUMBER, ResponseMessage.DUPLICATE_PHONE_NUMBER);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}
