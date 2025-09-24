package com.tetz.spring_proj.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetz.spring_proj.auth.dto.AuthResponse;
import com.tetz.spring_proj.auth.dto.KakaoUserInfo;
import com.tetz.spring_proj.common.security.auth.JwtUtil;
import com.tetz.spring_proj.user.service.UserService;
import com.tetz.spring_proj.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthResponse processKakaoLogin(String accessToken) {
        try {
            // 1. 카카오 사용자 정보 조회
            KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(accessToken);

            // 2. 사용자 처리는 UserService에 위임
            UserEntity user = userService.findOrCreateByKakaoInfo(kakaoUserInfo);

            // 3. JWT 토큰 생성 (Auth의 책임)
            String jwtToken = jwtUtil.generateToken(user.getId(), user.getEmail());

            return AuthResponse.builder()
                    .accessToken(jwtToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration / 1000) // 밀리초를 초로 변환
                    .userInfo(AuthResponse.UserInfo.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .nickname(user.getNickname())
                            .profileImageUrl(user.getProfileImageUrl())
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류 발생", e);
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.");
        }
    }

    private KakaoUserInfo getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                kakaoUserInfoUri,
                HttpMethod.GET,
                entity,
                String.class
        );

        try {
            return objectMapper.readValue(response.getBody(), KakaoUserInfo.class);
        } catch (Exception e) {
            log.error("카카오 사용자 정보 파싱 오류", e);
            throw new RuntimeException("사용자 정보 조회에 실패했습니다.");
        }
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public Long getUserIdFromToken(String token) {
        String userIdStr = jwtUtil.getUserIdFromToken(token);
        return userIdStr != null ? Long.valueOf(userIdStr) : null;
    }
}
