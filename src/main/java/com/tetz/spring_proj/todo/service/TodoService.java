package com.tetz.spring_proj.todo.service;

import com.tetz.spring_proj.todo.domain.TodoEntity;
import com.tetz.spring_proj.todo.repository.TodoRepository;
import com.tetz.spring_proj.user.domain.UserEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {
    private final TodoRepository todoRepository;

    public List<TodoEntity> getAllTodos(UserEntity user) {
        return todoRepository.findAllTodosByUser(user);
    }

    public List<TodoEntity> getIncompleteTodos(UserEntity user) {
        return todoRepository.findIncompleteTodosByUser(user);
    }

    @Transactional
    public TodoEntity createTodo(String task, UserEntity user) {
        if (task == null || task.trim().isEmpty()) {
            throw new IllegalArgumentException("할일 내용은 필수입니다.");
        }

        TodoEntity todo = TodoEntity.builder()
                .task(task.trim())
                .user(user)
                .build();

        return todoRepository.save(todo);
    }

    @Transactional
    public TodoEntity updateTask(Long todoId, String newTask, UserEntity user) {
        if (newTask == null || newTask.trim().isEmpty()) {
            throw new IllegalArgumentException("할일 내용은 필수입니다.");
        }

        TodoEntity todo = todoRepository.findTodoByIdAndUser(todoId, user)
                .orElseThrow(() -> new EntityNotFoundException("할일을 찾을 수 없습니다."));

        todo.updateTask(newTask.trim());
        return todo;
    }

    @Transactional
    public TodoEntity markAsCompleted(Long todoId, UserEntity user) {
        TodoEntity todo = todoRepository.findTodoByIdAndUser(todoId, user)
                .orElseThrow(() -> new EntityNotFoundException("할일을 찾을 수 없습니다."));

        todo.markAsCompleted();
        return todo;
    }

    @Transactional
    public TodoEntity markAsIncomplete(Long todoId, UserEntity user) {
        TodoEntity todo = todoRepository.findTodoByIdAndUser(todoId, user)
                .orElseThrow(() -> new EntityNotFoundException("할일을 찾을 수 없습니다."));

        todo.markAsIncomplete();
        return todo;
    }

    @Transactional
    public void deleteTodo(Long todoId, UserEntity user) {
        TodoEntity todo = todoRepository.findTodoByIdAndUser(todoId, user)
                .orElseThrow(() -> new EntityNotFoundException("할일을 찾을 수 없습니다."));

        todoRepository.delete(todo);
    }
}