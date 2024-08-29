package com.inconcert.domain.user.dto.response;

import com.inconcert.common.dto.ResponseDto;
import com.inconcert.common.exception.ResponseCode;
import com.inconcert.common.exception.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class NicknameCheckRspDto extends ResponseDto {
    public NicknameCheckRspDto() {
        super();
    }

    public static ResponseEntity<NicknameCheckRspDto> success() {
        NicknameCheckRspDto responseBody = new NicknameCheckRspDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> duplicateNickname() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.DUPLICATE_NICKNAME, ResponseMessage.DUPLICATE_NICKNAME);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}
