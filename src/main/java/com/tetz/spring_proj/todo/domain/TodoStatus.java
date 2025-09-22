package com.tetz.spring_proj.todo.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum TodoStatus {
    IN_PROGRESS("진행중"),
    COMPLETED("완료");

    private final String description;

    TodoStatus(String description) {
        this.description = description;
    }
}
