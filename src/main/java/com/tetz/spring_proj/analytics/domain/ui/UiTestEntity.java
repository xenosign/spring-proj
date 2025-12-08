package com.tetz.spring_proj.analytics.domain.ui;

import com.tetz.spring_proj.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_ui")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UiTestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false)
    private Long stayTime;


    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Builder
    public UiTestEntity(String type, Long stayTime, UserEntity user) {
        this.type = type;
        this.stayTime = stayTime;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }
}
