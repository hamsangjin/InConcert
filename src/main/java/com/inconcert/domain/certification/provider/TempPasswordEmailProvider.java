package com.inconcert.domain.certification.provider;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TempPasswordEmailProvider {
    private final JavaMailSender mailSender;
    private final String SUBJECT = "[InConcert] 임시 비밀번호입니다.";

    // 인증 메일 전송
    public boolean sendEmail(String email, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // html 형식 메시지

            String htmlContent = getCertificationNumber(tempPassword);

            helper.setTo(email);    // 메일 전송
            helper.setSubject(SUBJECT);
            helper.setText(htmlContent, true);  // html 적용하여 메시지 전송

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    // 메일 본문
    private String getCertificationNumber(String tempPassword) {
        return """
                <h1 style="text-align: center">[InConcert] 인증 메일</h1>
                <h3 style="text-align: center">임시 비밀번호: <strong style="font-size: 32px; letter-spacing: 8px">
                """ +
                tempPassword +
                "</strong></h3>";
    }
}
