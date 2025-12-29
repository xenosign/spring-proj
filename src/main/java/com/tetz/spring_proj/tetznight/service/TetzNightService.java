package com.tetz.spring_proj.tetznight.service;

import com.tetz.spring_proj.tetznight.domain.vote.Vote;
import com.tetz.spring_proj.tetznight.repository.VoteRepository;
import com.tetz.spring_proj.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TetzNightService {
    private final VoteRepository voteRepository;
    private final UserService userService;

    @Transactional
    public void vote(Long userId, VoteRequest request) {
        // userId 검증은 애플리케이션 레벨에서
        if (userService.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다");
        }

        Vote vote = new Vote();
        vote.setUserId(userId);  // ✅ 숫자만 설정
        vote.setPoll(poll);
        vote.setPollOption(option);
        voteRepository.save(vote);
    }
}
