package com.inconcert.domain.user.dto.response;

import com.inconcert.global.dto.ResponseDto;
import com.inconcert.global.exception.ResponseCode;
import com.inconcert.global.exception.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RegisterRspDto extends ResponseDto {
    public RegisterRspDto() {
        super();
    }

    public static ResponseEntity<RegisterRspDto> success() {
        RegisterRspDto responseBody = new RegisterRspDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> duplicateId() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.DUPLICATE_ID, ResponseMessage.DUPLICATE_ID);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> passwordNotMatch() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.PASSWORD_NOT_MATCH, ResponseMessage.PASSWORD_NOT_MATCH);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> certificationFail() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.CERTIFICATION_FAIL, ResponseMessage.CERTIFICATION_FAIL);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }
}
