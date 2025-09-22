    package com.tetz.spring_proj.todo.domain;

    import jakarta.persistence.*;
    import lombok.AccessLevel;
    import lombok.Builder;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import org.springframework.data.annotation.CreatedDate;
    import org.springframework.data.annotation.LastModifiedDate;
    import org.springframework.data.jpa.domain.support.AuditingEntityListener;

    import java.time.LocalDateTime;

    @Entity
    @Table(name = "todos")
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @EntityListeners(AuditingEntityListener.class)
    public class Todo {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 500)
        private String title;

        @Column(length = 2000)
        private String description;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TodoStatus status = TodoStatus.IN_PROGRESS;

        @Column(name = "due_date")
        private LocalDateTime dueDate;

        @CreatedDate
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @LastModifiedDate
        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        // 생성자
        @Builder
        private Todo(String title, String description, LocalDateTime dueDate) {
            this.title = title;
            this.description = description;
            this.dueDate = dueDate;
            this.status = TodoStatus.IN_PROGRESS;
        }

        // 비즈니스 메서드
        public void updateTitle(String title) {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("제목은 필수입니다.");
            }
            this.title = title;
        }

        public void updateDescription(String description) {
            this.description = description;
        }

        public void updateDueDate(LocalDateTime dueDate) {
            this.dueDate = dueDate;
        }

        public void markAsCompleted() {
            this.status = TodoStatus.COMPLETED;
        }

        public void markAsInProgress() {
            this.status = TodoStatus.IN_PROGRESS;
        }

        public boolean isCompleted() {
            return this.status == TodoStatus.COMPLETED;
        }
    }
