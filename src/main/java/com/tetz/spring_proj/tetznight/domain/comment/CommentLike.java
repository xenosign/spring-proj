package com.tetz.spring_proj.tetznight.domain.comment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_comment_user_like",
                columnNames = {"comment_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_comment_id", columnList = "comment_id"),
                @Index(name = "idx_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public CommentLike(Long commentId, Long userId) {
        this.commentId = commentId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
}