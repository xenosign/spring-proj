package com.tetz.spring_proj.tetznight.repository.comment;

import com.tetz.spring_proj.tetznight.domain.comment.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    /**
     * 특정 댓글에 특정 사용자가 좋아요를 눌렀는지 확인
     */
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    /**
     * 특정 댓글에 특정 사용자의 좋아요 조회
     */
    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

    /**
     * 특정 댓글의 좋아요 수
     */
    long countByCommentId(Long commentId);

    /**
     * 특정 댓글의 모든 좋아요 목록
     */
    List<CommentLike> findByCommentId(Long commentId);

    /**
     * 특정 사용자가 누른 모든 좋아요 목록
     */
    List<CommentLike> findByUserId(Long userId);

    /**
     * 특정 댓글에 특정 사용자의 좋아요 삭제
     */
    void deleteByCommentIdAndUserId(Long commentId, Long userId);
}