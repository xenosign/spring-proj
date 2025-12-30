package com.tetz.spring_proj.tetznight.service.comment;

import com.tetz.spring_proj.tetznight.domain.comment.Comment;
import com.tetz.spring_proj.tetznight.domain.comment.CommentLike;
import com.tetz.spring_proj.tetznight.dto.comment.CommentCreateRequest;
import com.tetz.spring_proj.tetznight.dto.comment.CommentLikeResponse;
import com.tetz.spring_proj.tetznight.dto.comment.CommentResponse;
import com.tetz.spring_proj.tetznight.repository.comment.CommentLikeRepository;
import com.tetz.spring_proj.tetznight.repository.comment.CommentRepository;
import com.tetz.spring_proj.tetznight.repository.poll.PollRepository;
import com.tetz.spring_proj.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PollRepository pollRepository;
    private final UserService userService;

    public String createComment(Long userId, CommentCreateRequest request) {
        if (userService.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다");
        }

        if (!pollRepository.existsById(request.getPollId())) {
            throw new IllegalArgumentException("존재하지 않는 투표입니다");
        }

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setPollId(request.getPollId());
        comment.setNickname(request.getNickname());
        comment.setComment(request.getComment());
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        log.info("댓글 작성 완료 - Comment ID: {}, User ID: {}, Poll ID: {}, Nickname: {}",
                savedComment.getId(), userId, request.getPollId(), request.getNickname());

        return "댓글 작성 완료";
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPollId(Long pollId) {
        if (!pollRepository.existsById(pollId)) {
            throw new IllegalArgumentException("존재하지 않는 투표입니다");
        }

        return commentRepository.findByPollIdOrderByCreatedAtAsc(pollId)
                .stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    public CommentLikeResponse toggleLike(Long commentId, Long userId) {
        if (userService.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다"));

        boolean alreadyLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);

        if (alreadyLiked) {
            commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
            comment.decrementLikeCount();
            commentRepository.save(comment);

            log.info("댓글 좋아요 취소 - Comment ID: {}, User ID: {}", commentId, userId);

            return CommentLikeResponse.of(commentId, comment.getLikeCount(), false);
        } else {
            CommentLike commentLike = new CommentLike(commentId, userId);
            commentLikeRepository.save(commentLike);
            comment.incrementLikeCount();
            commentRepository.save(comment);

            log.info("댓글 좋아요 추가 - Comment ID: {}, User ID: {}", commentId, userId);

            return CommentLikeResponse.of(commentId, comment.getLikeCount(), true);
        }
    }

    @Transactional(readOnly = true)
    public Integer getLikeCount(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다"));

        return comment.getLikeCount();
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long commentId, Long userId) {
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
    }

    @Transactional(readOnly = true)
    public CommentLikeResponse getLikeStatus(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다"));

        boolean isLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);

        return CommentLikeResponse.of(commentId, comment.getLikeCount(), isLiked);
    }
}