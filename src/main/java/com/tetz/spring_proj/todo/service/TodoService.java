package com.tetz.spring_proj.todo.service;

import com.tetz.spring_proj.todo.domain.TodoEntity;
import com.tetz.spring_proj.todo.dto.TodoResponseDto;
import com.tetz.spring_proj.todo.repository.TodoRepository;
import com.tetz.spring_proj.user.domain.UserEntity;
import com.tetz.spring_proj.user.repository.UserRepository; // 💡 UserRepository 추가 가정
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {
    private final TodoRepository todoRepository;
    private final UserRepository userRepository; // 💡 UserRepository 주입

    public List<TodoResponseDto> getAllTodosByUserId(String userId) {
        UserEntity user = findUserById(userId);
        return todoRepository.findAllTodosByUser(user)
                .stream()
                .map(TodoResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<TodoResponseDto> getIncompleteTodosByUserId(String userId) {
        UserEntity user = findUserById(userId);
        return todoRepository.findIncompleteTodosByUser(user)
                .stream()
                .map(TodoResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public TodoResponseDto createTodo(String task, String userId) {
        if (task == null || task.trim().isEmpty()) {
            throw new IllegalArgumentException("할일 내용은 필수입니다.");
        }
        UserEntity user = findUserById(userId); // 💡 사용자 조회

        TodoEntity todo = TodoEntity.builder()
                .task(task.trim())
                .user(user)
                .build();

        TodoEntity savedTodo = todoRepository.save(todo);
        return new TodoResponseDto(savedTodo);
    }

    @Transactional
    public TodoResponseDto markAsCompleted(Long todoId, String userId) {
        UserEntity user = findUserById(userId); // 💡 userId로 UserEntity 조회

        TodoEntity todo = todoRepository.findTodoByIdAndUser(todoId, user)
                .orElseThrow(() -> new EntityNotFoundException("할일을 찾을 수 없거나 접근 권한이 없습니다."));

        todo.markAsCompleted();
        return new TodoResponseDto(todo);
    }

    @Transactional
    public TodoResponseDto markAsIncomplete(Long todoId, String userId) {
        UserEntity user = findUserById(userId); // 💡 userId로 UserEntity 조회

        TodoEntity todo = todoRepository.findTodoByIdAndUser(todoId, user)
                .orElseThrow(() -> new EntityNotFoundException("할일을 찾을 수 없거나 접근 권한이 없습니다."));

        todo.markAsIncomplete();
        return new TodoResponseDto(todo);
    }

    @Transactional
    public TodoResponseDto updateTask(Long todoId, String newTask, String userId) {
        if (newTask == null || newTask.trim().isEmpty()) {
            throw new IllegalArgumentException("할일 내용은 필수입니다.");
        }

        UserEntity user = findUserById(userId);

        TodoEntity todo = todoRepository.findTodoByIdAndUser(todoId, user)
                .orElseThrow(() -> new EntityNotFoundException("할일을 찾을 수 없거나 접근 권한이 없습니다."));

        todo.updateTask(newTask.trim());
        return new TodoResponseDto(todo);
    }

    @Transactional
    public void deleteTodo(Long todoId, String userId) {
        UserEntity user = findUserById(userId);
        TodoEntity todo = todoRepository.findTodoByIdAndUser(todoId, user)
                .orElseThrow(() -> new EntityNotFoundException("할일을 찾을 수 없습니다."));

        todoRepository.delete(todo);
    }

    private UserEntity findUserById(String userId) {
        Long id = Long.parseLong(userId);
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }
}