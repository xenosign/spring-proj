package com.tetz.spring_proj.todo.dto;

import com.tetz.spring_proj.todo.domain.TodoEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TodoResponseDto {
    private final Long id;
    private final String task;
    private final Boolean completed;
    private final LocalDateTime createdAt;

    public TodoResponseDto(TodoEntity todoEntity) {
        this.id = todoEntity.getId();
        this.task = todoEntity.getTask();
        this.completed = todoEntity.getCompleted();
        this.createdAt = todoEntity.getCreatedAt();
    }
}
