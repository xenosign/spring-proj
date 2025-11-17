package com.tetz.spring_proj.auth.util;

import com.tetz.spring_proj.common.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationUtil {

    private final JwtUtil jwtUtil;

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return Long.valueOf((String) authentication.getPrincipal());
        }
        return null;
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof String) {
            return (String) authentication.getDetails();
        }
        return null;
    }

    public Long getUserIdFromToken(String token) {
        String userIdStr = jwtUtil.getUserIdFromToken(token);
        return userIdStr != null ? Long.valueOf(userIdStr) : null;
    }

    public String getEmailFromToken(String token) {
        return jwtUtil.getEmailFromToken(token);
    }
}
