    package com.tetz.spring_proj.todo.domain;

    import com.tetz.spring_proj.user.domain.UserEntity;
    import jakarta.persistence.*;
    import lombok.AccessLevel;
    import lombok.Builder;
    import lombok.Getter;
    import lombok.NoArgsConstructor;

    import java.time.LocalDateTime;

    @Entity
    @Table(name = "todos")
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public class TodoEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 500)
        private String task;  // 할일

        @Column(nullable = false)
        private Boolean completed = false;  // 완료 여부

        @Column(nullable = false)
        private LocalDateTime updatedAt;  // 최종 업데이트 일

        @Builder
        public TodoEntity(String task, UserEntity user) {
            this.task = task;
            this.completed = false;
            this.user = user;
            this.updatedAt = LocalDateTime.now();
        }

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private UserEntity user;  // 사용자 연관관계

        @PreUpdate
        protected void onUpdate() {
            this.updatedAt = LocalDateTime.now();
        }

        // 완료 상태 토글
        public void toggleCompleted() {
            this.completed = !this.completed;
        }

        // 할일 내용 수정
        public void updateTask(String task) {
            if (task != null && !task.trim().isEmpty()) {
                this.task = task;
            }
        }

        // 완료 처리
        public void markAsCompleted() {
            this.completed = true;
        }

        // 미완료 처리
        public void markAsIncomplete() {
            this.completed = false;
        }
    }