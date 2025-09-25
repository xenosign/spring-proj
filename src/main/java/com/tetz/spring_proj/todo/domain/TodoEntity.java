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
        private String task;

        @Column(nullable = false)
        private Boolean completed = false;

        @Column(nullable = false)
        private LocalDateTime updatedAt;

        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Builder
        public TodoEntity(String task, UserEntity user) {
            this.task = task;
            this.completed = false;
            this.user = user;
        }

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private UserEntity user;  // 사용자 연관관계

        @PrePersist
        protected void onCreate() {
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }

        @PreUpdate
        protected void onUpdate() {
            this.updatedAt = LocalDateTime.now();
        }

        public void toggleCompleted() {
            this.completed = !this.completed;
        }

        public void updateTask(String task) {
            if (task != null && !task.trim().isEmpty()) {
                this.task = task;
            }
        }

        public void markAsCompleted() {
            this.completed = true;
        }

        public void markAsIncomplete() {
            this.completed = false;
        }
    }