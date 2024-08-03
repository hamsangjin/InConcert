package com.inconcert.domain.user.dto.response;

import com.inconcert.global.dto.ResponseDto;
import com.inconcert.global.exception.ResponseCode;
import com.inconcert.global.exception.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CheckCertificationRspDto extends ResponseDto {
    private CheckCertificationRspDto() {
        super();
    }

    public static ResponseEntity<CheckCertificationRspDto> success() {
        CheckCertificationRspDto responseBody = new CheckCertificationRspDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> certificationFail() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.CERTIFICATION_FAIL, ResponseMessage.CERTIFICATION_FAIL);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }
}
