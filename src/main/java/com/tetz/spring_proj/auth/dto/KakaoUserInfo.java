package com.tetz.spring_proj.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfo {
    private Long id;
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @JsonProperty("properties")     // 카카오 API에서 properties도 제공
    private Properties properties;

    @Getter
    @NoArgsConstructor
    public static class KakaoAccount {
        private String email;
        private Profile profile;

        @Getter
        @NoArgsConstructor
        public static class Profile {
            private String nickname;
            private String profileImageUrl;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class Properties {
        private String nickname;

        @JsonProperty("profile_image")
        private String profileImage;
    }
}
