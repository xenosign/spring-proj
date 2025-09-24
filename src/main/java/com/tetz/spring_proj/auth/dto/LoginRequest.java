package com.tetz.spring_proj.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {
    private String accessToken;
    private String provider; // "kakao", "google" 등
}
