package com.tetz.spring_proj.auth.controller;

import com.tetz.spring_proj.auth.dto.AuthResponse;
import com.tetz.spring_proj.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Value("${kakao.cookie-secure}")
    private boolean kakaoCookieSecure;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${kakao.front.redirect.url}")
    private String kakaoFrontRedirectUrl;

    @GetMapping("/kakao")
    public RedirectView kakaoLogin() {
        String kakaoLoginUrl = authService.getKakaoLoginUrl();
        return new RedirectView(kakaoLoginUrl);
    }

    // JWT 토큰 백엔드 확인용 엔드 포인트
    @GetMapping("/kakao/backend")
    public RedirectView kakaoLoginForBackend() {
        String kakaoLoginUrl = authService.getKakaoLoginUrl("backend");
        return new RedirectView(kakaoLoginUrl);
    }

    @GetMapping("/kakao/callback")
    public Object kakaoCallback(@RequestParam String code,
                                @RequestParam(required = false) String state,
                                HttpServletResponse response) {

        try {
            AuthResponse authResponse = authService.processKakaoCallback(code);

            Cookie jwtCookie = new Cookie("jwt", authResponse.getAccessToken());
            jwtCookie.setHttpOnly(true); // XSS 공격 방지
            jwtCookie.setSecure(kakaoCookieSecure);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge((int) (jwtExpiration / 1000));
            response.addCookie(jwtCookie);

            if ("backend".equals(state)) {
                return ResponseEntity.ok(authResponse);
            } else {
                return new RedirectView(kakaoFrontRedirectUrl);
            }

        } catch (Exception e) {
            log.error("카카오 OAuth 콜백 처리 오류", e);

            if ("backend".equals(state)) {
                return ResponseEntity.badRequest().build();
            } else {
                return new RedirectView(kakaoFrontRedirectUrl + "?error=auth_failed");
            }
        }
    }


    @GetMapping("/kakao/url")
    public ResponseEntity<Map<String, String>> getKakaoLoginUrl() {
        String kakaoLoginUrl = authService.getKakaoLoginUrl();
        return ResponseEntity.ok(Map.of("loginUrl", kakaoLoginUrl));
    }

    @PostMapping("/kakao")
    public ResponseEntity<AuthResponse> kakaoLogin(@RequestParam String accessToken,
                                                   HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.processKakaoLogin(accessToken);

            // JWT 토큰을 쿠키에 설정
            Cookie jwtCookie = new Cookie("jwt", authResponse.getAccessToken());
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true); // 개발환경에서는 false
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge((int) (jwtExpiration / 1000));

            response.addCookie(jwtCookie);

            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("카카오 로그인 오류", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // JWT 쿠키 삭제
        Cookie jwtCookie = new Cookie("jwt", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // 개발환경에서는 false
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // 즉시 만료

        response.addCookie(jwtCookie);

        return ResponseEntity.ok().build();
    }
}