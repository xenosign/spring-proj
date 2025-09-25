package com.tetz.spring_proj.todo.controller;

import com.tetz.spring_proj.todo.domain.TodoEntity;
import com.tetz.spring_proj.todo.service.TodoService;
import com.tetz.spring_proj.user.domain.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "TODO LIST API", description = "개인용 TODO LIST API")
@RestController
@RequestMapping("/api/v1/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    /**
     * 모든 할일 조회
     */
    @Operation(summary = "TODO LIST 조회", description = "사용자의 모든 TODO LIST 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @GetMapping("/")
    public ResponseEntity<List<TodoEntity>> getAllTodos(HttpSession session) {
        UserEntity user = getUserFromSession(session);
        List<TodoEntity> todos = todoService.getAllTodos(user);
        return ResponseEntity.ok(todos);
    }

    /**
     * 미완료 할일만 조회
     */
    @Operation(summary = "미완료 TODO LIST 조회", description = "사용자의 미완료된 TODO LIST 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @GetMapping("/incomplete")
    public ResponseEntity<List<TodoEntity>> getIncompleteTodos(HttpSession session) {
        UserEntity user = getUserFromSession(session);
        List<TodoEntity> todos = todoService.getIncompleteTodos(user);
        return ResponseEntity.ok(todos);
    }

    /**
     * 새 할일 생성
     */
    @Operation(summary = "TODO 생성", description = "새로운 TODO 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @PostMapping("/")
    public ResponseEntity<TodoEntity> createTodo(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        UserEntity user = getUserFromSession(session);
        String task = request.get("task");

        TodoEntity createdTodo = todoService.createTodo(task, user);
        return ResponseEntity.ok(createdTodo);
    }

    /**
     * 할일 내용 수정
     */
    @Operation(summary = "TODO 수정", description = "TODO 내용 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "TODO를 찾을 수 없음")
    })
    @PutMapping("/{todoId}")
    public ResponseEntity<TodoEntity> updateTodo(
            @Parameter(description = "TODO ID", required = true) @PathVariable Long todoId,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        UserEntity user = getUserFromSession(session);
        String newTask = request.get("task");

        TodoEntity updatedTodo = todoService.updateTask(todoId, newTask, user);
        return ResponseEntity.ok(updatedTodo);
    }

    /**
     * 할일 완료 처리
     */
    @Operation(summary = "TODO 완료 처리", description = "TODO를 완료 상태로 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "완료 처리 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "TODO를 찾을 수 없음")
    })
    @PatchMapping("/{todoId}/complete")
    public ResponseEntity<TodoEntity> markAsCompleted(
            @Parameter(description = "TODO ID", required = true) @PathVariable Long todoId,
            HttpSession session) {

        UserEntity user = getUserFromSession(session);
        TodoEntity todo = todoService.markAsCompleted(todoId, user);
        return ResponseEntity.ok(todo);
    }

    /**
     * 할일 미완료 처리
     */
    @Operation(summary = "TODO 미완료 처리", description = "TODO를 미완료 상태로 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미완료 처리 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "TODO를 찾을 수 없음")
    })
    @PatchMapping("/{todoId}/incomplete")
    public ResponseEntity<TodoEntity> markAsIncomplete(
            @Parameter(description = "TODO ID", required = true) @PathVariable Long todoId,
            HttpSession session) {

        UserEntity user = getUserFromSession(session);
        TodoEntity todo = todoService.markAsIncomplete(todoId, user);
        return ResponseEntity.ok(todo);
    }

    /**
     * 할일 삭제
     */
    @Operation(summary = "TODO 삭제", description = "TODO 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "TODO를 찾을 수 없음")
    })
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(
            @Parameter(description = "TODO ID", required = true) @PathVariable Long todoId,
            HttpSession session) {

        UserEntity user = getUserFromSession(session);
        todoService.deleteTodo(todoId, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * 세션에서 사용자 정보 가져오기
     */
    private UserEntity getUserFromSession(HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return user;
    }
}