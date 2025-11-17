package com.tetz.spring_proj.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetz.spring_proj.auth.dto.AuthResponse;
import com.tetz.spring_proj.auth.dto.KakaoUserInfo;
import com.tetz.spring_proj.auth.dto.KakaoTokenResponse;
import com.tetz.spring_proj.security.auth.JwtUtil;
import com.tetz.spring_proj.user.service.UserService;
import com.tetz.spring_proj.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.auth-uri}")
    private String kakaoAuthUri;

    @Value("${kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String getKakaoLoginUrl() {
        return getKakaoLoginUrl(null);
    }

    public String getKakaoLoginUrl(String state) {
         UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(kakaoAuthUri)
                .queryParam("client_id", kakaoClientId)
                .queryParam("redirect_uri", kakaoRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "profile_nickname,profile_image,account_email");

        if (state != null && !state.trim().isEmpty()) {
            builder.queryParam("state", state);
        }

        return builder.build().toUriString();
    }

    public AuthResponse processKakaoCallback(String authorizationCode) {
        try {
            String accessToken = getKakaoAccessToken(authorizationCode);

            return processKakaoLogin(accessToken);
        } catch (Exception e) {
            log.error("카카오 OAuth 로그인 처리 중 오류 발생", e);
            throw new RuntimeException("OAuth 로그인 처리 중 오류가 발생했습니다.");
        }
    }

    private String getKakaoAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", authorizationCode);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    kakaoTokenUri,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            KakaoTokenResponse tokenResponse = objectMapper.readValue(
                    response.getBody(),
                    KakaoTokenResponse.class
            );

            return tokenResponse.getAccessToken();

        } catch (Exception e) {
            log.error("카카오 액세스 토큰 발급 오류", e);
            throw new RuntimeException("액세스 토큰 발급에 실패했습니다.");
        }
    }

    public AuthResponse processKakaoLogin(String accessToken) {
        try {
            KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(accessToken);

            UserEntity user = userService.findOrCreateByKakaoInfo(kakaoUserInfo);

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