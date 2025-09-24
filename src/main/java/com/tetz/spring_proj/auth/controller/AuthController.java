package com.tetz.spring_proj.auth.controller;

import com.tetz.spring_proj.auth.dto.AuthResponse;
import com.tetz.spring_proj.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/kakao")
    public ResponseEntity<AuthResponse> kakaoLogin(@RequestParam String accessToken) {
        try {
            AuthResponse response = authService.processKakaoLogin(accessToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("카카오 로그인 오류", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // 로그아웃 로직 (JWT는 stateless이므로 클라이언트에서 토큰 삭제)
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {
        // 리프레시 토큰 로직 (필요시 구현)
        return ResponseEntity.ok().build();
    }
}
