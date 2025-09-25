package com.tetz.spring_proj.todo.controller;

import com.tetz.spring_proj.todo.dto.TodoRequestDto;
import com.tetz.spring_proj.todo.dto.TodoResponseDto;
import com.tetz.spring_proj.todo.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 💡 추가
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "TODO LIST API", description = "개인용 TODO LIST API")
@RestController
@RequestMapping("/api/v1/todo")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;

    @Operation(summary = "TODO LIST 조회", description = "사용자의 모든 TODO LIST 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @GetMapping
    public ResponseEntity<List<TodoResponseDto>> getAllTodos(
            @AuthenticationPrincipal String userId) {
        List<TodoResponseDto> todos = todoService.getAllTodosByUserId(userId);
        return ResponseEntity.ok(todos);
    }

    @Operation(summary = "미완료 TODO LIST 조회", description = "사용자의 미완료된 TODO LIST 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @GetMapping("/incomplete")
    public ResponseEntity<List<TodoResponseDto>> getIncompleteTodos(
            @AuthenticationPrincipal String userId) {
        List<TodoResponseDto> todos = todoService.getIncompleteTodosByUserId(userId);
        return ResponseEntity.ok(todos);
    }

    @Operation(summary = "TODO 생성", description = "새로운 TODO를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    @PostMapping
    public ResponseEntity<TodoResponseDto> createTodo(
            @RequestBody TodoRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal String userId) {
        TodoResponseDto createdTodo = todoService.createTodo(requestDto.getTask(), userId);
        return ResponseEntity.ok(createdTodo);
    }

    @Operation(summary = "TODO 수정", description = "기존 TODO의 내용을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "TODO를 찾을 수 없음")
    })
    @PutMapping("/{todoId}")
    public ResponseEntity<TodoResponseDto> updateTodo(
            @Parameter(description = "수정할 TODO의 ID", required = true) @PathVariable Long todoId,
            @RequestBody TodoRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal String userId) {
        TodoResponseDto updatedTodo = todoService.updateTask(todoId, requestDto.getTask(), userId);
        return ResponseEntity.ok(updatedTodo);
    }

    @Operation(summary = "TODO 완료 처리", description = "TODO를 완료 상태로 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "완료 처리 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "TODO를 찾을 수 없음")
    })
    @PatchMapping("/{todoId}/complete")
    public ResponseEntity<TodoResponseDto> markAsCompleted(
            @Parameter(description = "TODO ID", required = true) @PathVariable Long todoId,
            @AuthenticationPrincipal String userId) {
        TodoResponseDto todo = todoService.markAsCompleted(todoId, userId);
        return ResponseEntity.ok(todo);
    }

    @Operation(summary = "TODO 미완료 처리", description = "TODO를 미완료 상태로 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미완료 처리 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "TODO를 찾을 수 없음")
    })
    @PatchMapping("/{todoId}/incomplete")
    public ResponseEntity<TodoResponseDto> markAsIncomplete(
            @Parameter(description = "TODO ID", required = true) @PathVariable Long todoId,
            @AuthenticationPrincipal String userId) {
        TodoResponseDto todo = todoService.markAsIncomplete(todoId, userId);
        return ResponseEntity.ok(todo);
    }

    @Operation(summary = "TODO 삭제", description = "TODO 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "404", description = "TODO를 찾을 수 없음")
    })
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(
            @Parameter(description = "TODO ID", required = true) @PathVariable Long todoId,
            @AuthenticationPrincipal String userId) {
        todoService.deleteTodo(todoId, userId);
        return ResponseEntity.noContent().build();
    }
}