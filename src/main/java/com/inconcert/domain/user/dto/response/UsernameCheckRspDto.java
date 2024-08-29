package com.inconcert.domain.user.dto.response;

import com.inconcert.common.dto.ResponseDto;
import com.inconcert.common.exception.ResponseCode;
import com.inconcert.common.exception.ResponseMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class UsernameCheckRspDto extends ResponseDto{
    public UsernameCheckRspDto() {
        super();
    }

    public static ResponseEntity<UsernameCheckRspDto> success() {
        UsernameCheckRspDto responseBody = new UsernameCheckRspDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> duplicateId() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.DUPLICATE_ID, ResponseMessage.DUPLICATE_ID);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}
