package com.tetz.spring_proj.tetznight.service.poll;

import com.tetz.spring_proj.tetznight.domain.poll.Poll;
import com.tetz.spring_proj.tetznight.domain.poll.PollOption;
import com.tetz.spring_proj.tetznight.domain.poll.Vote;
import com.tetz.spring_proj.tetznight.dto.poll.PollCreateRequest;
import com.tetz.spring_proj.tetznight.dto.poll.PollResponse;
import com.tetz.spring_proj.tetznight.dto.poll.UserVoteResponse;
import com.tetz.spring_proj.tetznight.dto.poll.VoteRequest;
import com.tetz.spring_proj.tetznight.repository.poll.PollOptionRepository;
import com.tetz.spring_proj.tetznight.repository.poll.PollRepository;
import com.tetz.spring_proj.tetznight.repository.poll.VoteRepository;
import com.tetz.spring_proj.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PollService {
    private final VoteRepository voteRepository;
    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final UserService userService;

    public PollResponse createPoll(Long userId, PollCreateRequest request) {
        if (userService.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다");
        }

        log.info("투표 생성 요청 - Poll Request: {}, 요청자: {}", request, userId);

        Poll poll = new Poll();
        poll.setTitle(request.getTitle());
        poll.setDescription(request.getDescription());
        poll.setStartDate(request.getStartDate());
        poll.setEndDate(request.getEndDate());
        poll.setAllowChangeVote(request.getAllowChangeVote());
        poll.setCreatedBy(userId);
        poll.setIsActive(true);
        poll.setCreatedAt(LocalDateTime.now());

        Poll savedPoll = pollRepository.save(poll);

        List<PollOption> options = request.getOptions().stream()
                .map(optionRequest -> {
                    PollOption option = new PollOption();
                    option.setPoll(savedPoll);
                    option.setOptionText(optionRequest.getOptionText());
                    option.setDisplayOrder(
                            optionRequest.getDisplayOrder() != null
                                    ? optionRequest.getDisplayOrder()
                                    : 0
                    );
                    return option;
                })
                .collect(Collectors.toList());

        List<PollOption> savedOptions = pollOptionRepository.saveAll(options);
        savedPoll.setOptions(savedOptions);

        log.info("투표 생성 완료 - Poll ID: {}, 생성자: {}", savedPoll.getId(), userId);

        return PollResponse.from(savedPoll);
    }

    public void vote(Long userId, VoteRequest request) {
        if (userService.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다");
        }

        Poll poll = pollRepository.findById(request.getPollId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표입니다"));

        if (!poll.getIsActive()) {
            throw new IllegalStateException("비활성화된 투표입니다");
        }

        PollOption option = pollOptionRepository.findById(request.getOptionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션입니다"));

        if (!option.getPoll().getId().equals(poll.getId())) {
            throw new IllegalArgumentException("해당 투표의 옵션이 아닙니다");
        }

        Optional<Vote> existingVote = voteRepository.findByPollIdAndUserId(
                request.getPollId(), userId);

        if (existingVote.isPresent()) {
            if (!poll.getAllowChangeVote()) {
                throw new IllegalStateException("투표 변경이 허용되지 않습니다");
            }

            Vote vote = existingVote.get();
            vote.setPollOption(option);
            voteRepository.save(vote);

            log.info("투표 변경 완료 - Vote ID: {}, User ID: {}, Option ID: {}",
                    vote.getId(), userId, option.getId());
        } else {
            Vote newVote = new Vote();
            newVote.setPoll(poll);
            newVote.setPollOption(option);
            newVote.setUserId(userId);
            newVote.setVotedAt(LocalDateTime.now());
            newVote.setUpdatedAt(LocalDateTime.now());
            newVote.setChangeCount(0);

            voteRepository.save(newVote);

            log.info("새 투표 완료 - User ID: {}, Poll ID: {}, Option ID: {}",
                    userId, poll.getId(), option.getId());
        }
    }

    @Transactional(readOnly = true)
    public List<PollResponse> getActivePolls() {
        return pollRepository.findByIsActiveTrueOrderByCreatedAtAsc()
                .stream()
                .map(poll -> {
                    List<PollOption> options = pollOptionRepository.findByPollIdOrderByDisplayOrder(poll.getId());
                    int totalVotes = (int) voteRepository.countByPollId(poll.getId());

                    return PollResponse.withVoteCounts(poll, options, totalVotes, null);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PollResponse getPollDetailById(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표입니다"));

        List<PollOption> options = pollOptionRepository.findByPollIdOrderByDisplayOrder(pollId);
        int totalVotes = (int) voteRepository.countByPollId(pollId);

        return PollResponse.withVoteCounts(poll, options, totalVotes, null);
    }

    @Transactional(readOnly = true)
    public PollResponse getPollDetailByIdWithUser(Long pollId, Long userId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 투표입니다"));

        List<PollOption> options = pollOptionRepository.findByPollIdOrderByDisplayOrder(pollId);
        int totalVotes = (int) voteRepository.countByPollId(pollId);

        // 사용자가 투표한 옵션 ID
        Long userVotedOptionId = null;
        if (userId != null) {
            userVotedOptionId = voteRepository.findByPollIdAndUserId(pollId, userId)
                    .map(vote -> vote.getPollOption().getId())
                    .orElse(null);
        }

        return PollResponse.withVoteCounts(poll, options, totalVotes, userVotedOptionId);
    }

    @Transactional(readOnly = true)
    public UserVoteResponse getUserVote(Long pollId, Long userId) {
        // 투표 존재 여부 확인
        if (!pollRepository.existsById(pollId)) {
            throw new IllegalArgumentException("존재하지 않는 투표입니다");
        }

        // 사용자 검증
        if (userService.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다");
        }

        // 사용자의 투표 조회
        Optional<Vote> voteOptional = voteRepository.findByPollIdAndUserId(pollId, userId);

        if (voteOptional.isPresent()) {
            Vote vote = voteOptional.get();
            PollOption option = vote.getPollOption();

            return UserVoteResponse.voted(pollId, option.getId(), option.getOptionText());
        } else {
            return UserVoteResponse.notVoted(pollId);
        }
    }

    @Transactional(readOnly = true)
    public Long getUserVotedOptionId(Long pollId, Long userId) {
        return voteRepository.findByPollIdAndUserId(pollId, userId)
                .map(vote -> vote.getPollOption().getId())
                .orElse(null);
    }
}