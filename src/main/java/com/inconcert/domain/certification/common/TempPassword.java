package com.inconcert.domain.certification.common;

public class TempPassword {
    // 임의의 여섯 자리 임시 비밀번호 생성
    public static String certificationNumber() {
        String certificationNumber = "";
        for(int i=0; i<6; i++) {
            certificationNumber += (int) (Math.random()*10);
        }

        return certificationNumber;
    }
}
