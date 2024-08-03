package com.inconcert.domain.user.dto.response;

import com.inconcert.global.dto.ResponseDto;
import com.inconcert.global.exception.ResponseCode;
import com.inconcert.global.exception.ResponseMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class EmailCertificationRspDto extends ResponseDto{ // 이메일 인증 response DTO
    public EmailCertificationRspDto() {
        super();
    }

    public static ResponseEntity<EmailCertificationRspDto> success() {
        EmailCertificationRspDto responseBody = new EmailCertificationRspDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> mailSendFail() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.MAIL_FAIL, ResponseMessage.MAIL_FAIL);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }
}
