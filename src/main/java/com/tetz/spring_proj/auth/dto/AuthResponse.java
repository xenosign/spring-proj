package com.tetz.spring_proj.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo userInfo;

    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String email;
        private String nickname;
        private String profileImageUrl;
    }
}
