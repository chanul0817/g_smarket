package com.gsmarket.controller;

import com.gsmarket.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @PostMapping("/sendVerificationEmail")
    public ResponseEntity<?> sendVerificationEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            mailService.sendMail(email); // 메일 전송 및 인증 코드 생성
            response.put("success", true);
            response.put("message", "인증 코드가 이메일로 전송되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verifyCode")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String userCode = request.get("userCode");
        String email = request.get("email");

        // 입력 값 검증
        if (userCode == null || email == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "인증 코드 또는 이메일이 누락되었습니다."));
        }

        // 인증 코드 검증
        boolean isVerified = mailService.verifyCode(email, userCode);
        if (isVerified) {
            return ResponseEntity.ok(Map.of("success", true, "message", "인증 성공!"));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "인증 코드가 올바르지 않거나 만료되었습니다."));
        }
    }

}
