package com.gsmarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private static final String senderEmail = "8sixman@gmail.com"; // 발신자 이메일 주소

    private static final ConcurrentHashMap<String, Integer> verificationCodes = new ConcurrentHashMap<>(); // 인증 코드 저장
    private static final ConcurrentHashMap<String, Boolean> verificationStatus = new ConcurrentHashMap<>(); // 인증 상태 저장
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1); // 만료 태스크 실행기

    private static final int EXPIRATION_TIME_MINUTES = 5; // 인증 코드 유효 시간 (분)

    private static int createNumber() {
        Random random = new Random();
        return random.nextInt(900000) + 100000; // 100000 ~ 999999
    }

    private MimeMessage createMail(String recipientEmail, int verificationNumber) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("이메일 인증 코드");

            String content = "<h3>요청하신 이메일 인증 코드입니다.</h3>" +
                    "<h1>" + verificationNumber + "</h1>" +
                    "<p>인증 코드를 입력해 주세요. 감사합니다!</p>";
            helper.setText(content, true);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 실패", e);
        }
        return message;
    }

    public int sendMail(String recipientEmail) {
        int verificationNumber = createNumber();
        MimeMessage mail = createMail(recipientEmail, verificationNumber);
        javaMailSender.send(mail);

        verificationCodes.put(recipientEmail, verificationNumber); // 인증 코드 저장
        verificationStatus.put(recipientEmail, false); // 초기 상태: 인증되지 않음
        scheduleCodeExpiration(recipientEmail);
        return verificationNumber;
    }

    private void scheduleCodeExpiration(String email) {
        executorService.schedule(() -> {
            verificationCodes.remove(email);
            verificationStatus.remove(email);
            System.out.println("Expired verification code for email: " + email);
        }, EXPIRATION_TIME_MINUTES, TimeUnit.MINUTES);
    }

    public boolean verifyCode(String email, String code) {
        if (email == null || code == null) {
            throw new IllegalArgumentException("이메일 또는 인증 코드가 null입니다.");
        }

        Integer generatedCode = verificationCodes.get(email);
        if (generatedCode != null && generatedCode.equals(Integer.parseInt(code))) {
            verificationStatus.put(email, true); // 인증 완료
            return true;
        }
        return false;
    }

    public boolean isEmailVerified(String email) {
        return verificationStatus.getOrDefault(email, false);
    }
}
