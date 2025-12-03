package com.tetz.spring_proj.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE) // 가장 먼저 실행되도록 설정
public class GlobalRequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        String cookieString = getCookieDetails(request);

        log.info("[{}] Request: {} {} (IP: {}) | Cookies: [{}]",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                cookieString
        );

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("[{}] Response: Status={} ({}ms)", requestId, response.getStatus(), duration);
        }
    }

    // 쿠키 배열을 보기 좋은 문자열로 변환하는 메서드
    private String getCookieDetails(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            return "No Cookies";
        }

        // 예: "jwt=abcde123, remember-me=true" 형태로 변환
        return Arrays.stream(cookies)
                .map(c -> c.getName() + "=" + c.getValue())
                .collect(Collectors.joining(", "));
    }
}