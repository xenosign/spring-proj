package com.tetz.spring_proj.tetznight.domain.vote;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "votes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_poll_user",
                columnNames = {"poll_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_poll_id", columnList = "poll_id"),
                @Index(name = "idx_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_option_id", nullable = false)
    private PollOption pollOption;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime votedAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(nullable = false)
    private Integer changeCount = 0;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.changeCount++;
    }
}
