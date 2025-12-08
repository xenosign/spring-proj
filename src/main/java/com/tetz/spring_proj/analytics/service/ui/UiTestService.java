package com.tetz.spring_proj.analytics.service.ui;

import com.tetz.spring_proj.analytics.domain.ui.UiTestEntity;
import com.tetz.spring_proj.analytics.dto.ui.UiTestRequestDto;
import com.tetz.spring_proj.analytics.repository.ui.UiTestRepository;
import com.tetz.spring_proj.auth.util.AuthenticationUtil;
import com.tetz.spring_proj.user.domain.UserEntity;
import com.tetz.spring_proj.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UiTestService {
    private final UserRepository userRepository;
    private final UiTestRepository uiTestRepository;

    @Transactional(readOnly = true)
    public void saveUiLog(Long userId, UiTestRequestDto dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        UiTestEntity uiTestEntity = UiTestEntity.builder()
                .user(user)
                .type(dto.getType())
                .stayTime(dto.getStayTime())
                .build();

        uiTestRepository.save(uiTestEntity);
    }
}
