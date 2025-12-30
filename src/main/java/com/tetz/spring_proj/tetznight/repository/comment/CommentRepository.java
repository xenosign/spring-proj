package com.tetz.spring_proj.tetznight.repository.comment;

import com.tetz.spring_proj.tetznight.domain.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPollIdOrderByCreatedAtAsc(Long pollId);

    List<Comment> findByPollIdOrderByCreatedAtDesc(Long pollId);
}