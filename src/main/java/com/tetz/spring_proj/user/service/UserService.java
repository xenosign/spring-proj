package com.tetz.spring_proj.user.service;

import com.tetz.spring_proj.auth.dto.KakaoUserInfo;
import com.tetz.spring_proj.user.domain.UserEntity;
import com.tetz.spring_proj.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByKakaoId(Long kakaoId) {
        return userRepository.findByKakaoId(kakaoId);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByKakaoId(Long kakaoId) {
        return userRepository.existsByKakaoId(kakaoId);
    }

    public UserEntity findOrCreateByKakaoInfo(KakaoUserInfo kakaoUserInfo) {
        return userRepository.findByKakaoId(kakaoUserInfo.getId())
                .map(user -> updateUserInfo(user, kakaoUserInfo))
                .orElseGet(() -> createUserFromKakaoInfo(kakaoUserInfo));
    }

    private UserEntity updateUserInfo(UserEntity user, KakaoUserInfo kakaoUserInfo) {
        // 기존 사용자 정보 업데이트
        String nickname = getNicknameFromKakaoInfo(kakaoUserInfo);
        String profileImageUrl = getProfileImageFromKakaoInfo(kakaoUserInfo);

        user.updateProfile(nickname, profileImageUrl);
        UserEntity updatedUser = userRepository.save(user);

        log.info("기존 사용자 정보 업데이트: kakaoId={}, nickname={}",
                updatedUser.getKakaoId(), updatedUser.getNickname());

        return updatedUser;
    }

    private UserEntity createUserFromKakaoInfo(KakaoUserInfo kakaoUserInfo) {
        String email = kakaoUserInfo.getKakaoAccount().getEmail();
        String nickname = getNicknameFromKakaoInfo(kakaoUserInfo);
        String profileImageUrl = getProfileImageFromKakaoInfo(kakaoUserInfo);

        UserEntity newUser = UserEntity.builder()
                .kakaoId(kakaoUserInfo.getId())
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();

        UserEntity savedUser = userRepository.save(newUser);
        log.info("새 사용자 생성: kakaoId={}, email={}, nickname={}",
                savedUser.getKakaoId(), savedUser.getEmail(), savedUser.getNickname());

        return savedUser;
    }

    // 카카오 정보에서 닉네임 추출 (우선순위: profile > properties)
    private String getNicknameFromKakaoInfo(KakaoUserInfo kakaoUserInfo) {
        // 1순위: kakao_account.profile.nickname
        if (kakaoUserInfo.getKakaoAccount() != null &&
                kakaoUserInfo.getKakaoAccount().getProfile() != null) {
            String profileNickname = kakaoUserInfo.getKakaoAccount().getProfile().getNickname();
            if (profileNickname != null && !profileNickname.trim().isEmpty()) {
                return profileNickname;
            }
        }

        // 2순위: properties.nickname
        if (kakaoUserInfo.getProperties() != null) {
            String propertiesNickname = kakaoUserInfo.getProperties().getNickname();
            if (propertiesNickname != null && !propertiesNickname.trim().isEmpty()) {
                return propertiesNickname;
            }
        }

        // 기본값
        return "카카오사용자";
    }

    // 카카오 정보에서 프로필 이미지 추출 (우선순위: profile > properties)
    private String getProfileImageFromKakaoInfo(KakaoUserInfo kakaoUserInfo) {
        // 1순위: kakao_account.profile.profile_image_url
        if (kakaoUserInfo.getKakaoAccount() != null &&
                kakaoUserInfo.getKakaoAccount().getProfile() != null) {
            String profileImageUrl = kakaoUserInfo.getKakaoAccount().getProfile().getProfileImageUrl();
            if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
                return profileImageUrl;
            }
        }

        // 2순위: properties.profile_image
        if (kakaoUserInfo.getProperties() != null) {
            String propertiesProfileImage = kakaoUserInfo.getProperties().getProfileImage();
            if (propertiesProfileImage != null && !propertiesProfileImage.trim().isEmpty()) {
                return propertiesProfileImage;
            }
        }

        // 기본값
        return null;
    }

    public UserEntity updateProfile(Long userId, String nickname, String profileImageUrl) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.updateProfile(nickname, profileImageUrl);
        UserEntity updatedUser = userRepository.save(user);

        log.info("사용자 프로필 업데이트: userId={}, nickname={}",
                updatedUser.getId(), updatedUser.getNickname());

        return updatedUser;
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        userRepository.deleteById(userId);
        log.info("사용자 삭제: userId={}", userId);
    }
}