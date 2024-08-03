package com.inconcert.domain.certification.common;

public class CertificationNumber {
    // 임의의 네 자리 인증번호 생성
    public static String certificationNumber() {
        String certificationNumber = "";
        for(int i=0; i<4; i++) {
            certificationNumber += (int) (Math.random()*10);
        }

        return certificationNumber;
    }
}