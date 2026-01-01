package com.tetz.spring_proj.tetznight.controller;

import com.tetz.spring_proj.tetznight.dto.comment.*;
import com.tetz.spring_proj.tetznight.dto.poll.*;
import com.tetz.spring_proj.tetznight.service.comment.CommentService;
import com.tetz.spring_proj.tetznight.service.poll.PollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "TetzNight API", description = "이효석의 밤 투표 및 댓글 API")
@RestController
@RequestMapping("/api/tetz")
@RequiredArgsConstructor
@Slf4j
public class TetzNightController {
    private final PollService pollService;
    private final CommentService commentService;

    @Operation(summary = "투표 생성", description = "새로운 투표를 생성 (로그인 필요)")
    @PostMapping("/polls")
    public ResponseEntity<PollResponse> createPoll(
            @Valid @RequestBody PollCreateRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {

        log.info("투표 생성 요청 - 사용자 ID: {}, 제목: {}", userId, request.getTitle());

        PollResponse response = pollService.createPoll(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "활성 투표 목록 조회", description = "활성화된 투표 목록을 조회")
    @GetMapping("/polls")
    public ResponseEntity<List<PollResponse>> getActivePolls() {
        log.info("활성 투표 목록 조회 요청");

        List<PollResponse> polls = pollService.getActivePolls();

        return ResponseEntity.ok(polls);
    }

    @Operation(summary = "투표 상세 조회", description = "특정 투표의 상세 정보와 투표 결과를 조회")
    @GetMapping("/polls/{pollId}")
    public ResponseEntity<PollResponse> getPollDetail(
            @Parameter(description = "투표 ID") @PathVariable Long pollId,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId) {

        log.info("투표 상세 조회 - Poll ID: {}, User ID: {}", pollId, userId);

        PollResponse response;
        if (userId != null) {
            response = pollService.getPollDetailByIdWithUser(pollId, userId);
        } else {
            response = pollService.getPollDetailById(pollId);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "투표하기", description = "특정 투표의 옵션에 투표 (로그인 필요)")
    @PostMapping("/votes")
    public ResponseEntity<Void> vote(
            @Valid @RequestBody VoteRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {

        log.info("투표 요청 - 사용자 ID: {}, Poll ID: {}, Option ID: {}",
                userId, request.getPollId(), request.getOptionId());

        pollService.vote(userId, request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 투표 조회", description = "특정 투표에서 자신이 투표한 옵션을 조회 (로그인 필요)")
    @GetMapping("/polls/{pollId}/my-vote")
    public ResponseEntity<UserVoteResponse> getMyVote(
            @Parameter(description = "투표 ID") @PathVariable Long pollId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {

        log.info("내 투표 조회 - Poll ID: {}, User ID: {}", pollId, userId);

        UserVoteResponse response = pollService.getUserVote(pollId, userId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 투표 옵션 ID 조회", description = "특정 투표에서 자신이 투표한 옵션 ID만 조회 (로그인 필요)")
    @GetMapping("/polls/{pollId}/my-vote/option-id")
    public ResponseEntity<Long> getMyVoteOptionId(
            @Parameter(description = "투표 ID") @PathVariable Long pollId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {

        log.info("내 투표 옵션 ID 조회 - Poll ID: {}, User ID: {}", pollId, userId);

        Long optionId = pollService.getUserVotedOptionId(pollId, userId);

        if (optionId == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(optionId);
    }

    @Operation(summary = "댓글 작성", description = "투표에 댓글을 작성 (로그인 필요)")
    @PostMapping("/comments")
    public ResponseEntity<String> createComment(
            @Valid @RequestBody CommentCreateRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {

        log.info("댓글 작성 요청 - 사용자 ID: {}, Poll ID: {}", userId, request.getPollId());

        String response = commentService.createComment(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "댓글 목록 조회", description = "특정 투표의 댓글 목록을 조회")
    @GetMapping("/polls/{pollId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPollId(
            @Parameter(description = "투표 ID") @PathVariable Long pollId) {

        log.info("댓글 목록 조회 - Poll ID: {}", pollId);

        List<CommentResponse> comments = commentService.getCommentsByPollId(pollId);

        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "댓글 좋아요 토글", description = "댓글에 좋아요를 추가하거나 취소합 (로그인 필요)")
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<CommentLikeResponse> toggleLike(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {

        log.info("댓글 좋아요 토글 - Comment ID: {}, User ID: {}", commentId, userId);

        CommentLikeResponse response = commentService.toggleLike(commentId, userId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 좋아요 수 조회", description = "특정 댓글의 좋아요 수를 조회")
    @GetMapping("/comments/{commentId}/like/count")
    public ResponseEntity<Integer> getLikeCount(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId) {

        log.info("댓글 좋아요 수 조회 - Comment ID: {}", commentId);

        Integer count = commentService.getLikeCount(commentId);

        return ResponseEntity.ok(count);
    }

    @Operation(summary = "댓글 좋아요 상태 조회", description = "댓글의 좋아요 수와 현재 사용자의 좋아요 여부를 조회 (로그인 필요)")
    @GetMapping("/comments/{commentId}/like/status")
    public ResponseEntity<CommentLikeResponse> getLikeStatus(
            @Parameter(description = "댓글 ID") @PathVariable Long commentId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {

        log.info("댓글 좋아요 상태 조회 - Comment ID: {}, User ID: {}", commentId, userId);

        CommentLikeResponse response = commentService.getLikeStatus(commentId, userId);

        return ResponseEntity.ok(response);
    }
}