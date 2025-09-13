package com.tetz.spring_proj.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  // 엔드 포인트
                        .allowedOrigins("http://localhost:3000", "http://localhost:5173") // Origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 메서드
                        .allowedHeaders("*") // 헤더
                        .allowCredentials(true); // 인증정보
            }
        };
    }
}

