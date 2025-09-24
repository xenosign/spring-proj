package com.tetz.spring_proj.user.controller;

import com.tetz.spring_proj.auth.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationUtil authenticationUtil;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Long userId = authenticationUtil.getCurrentUserId();
        String email = authenticationUtil.getCurrentUserEmail();

        return ResponseEntity.ok(
                "userId: " + userId + ", email: " + email
        );
    }
}