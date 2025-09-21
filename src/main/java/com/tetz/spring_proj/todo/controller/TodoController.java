package com.tetz.spring_proj.todo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "TODO LIST API", description = "개인용 TODO LIST API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/todo")
public class TodoController {
    @Operation(summary = "TODO LIST 조회", description = "사용자의 TODO LIST 조회")
    @GetMapping("/")
    public ResponseEntity<Void> getAllTodos() {
        return ResponseEntity.ok().build();
    }
}
