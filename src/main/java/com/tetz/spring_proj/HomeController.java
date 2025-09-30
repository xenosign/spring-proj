package com.tetz.spring_proj;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {
    @GetMapping(produces = "text/html")
    public String home() {
        return """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>kyucchoi</title>
            </head>
            <body>
                <a href="./swagger-ui/index.html" target="_blank">
                    <h1>🚀 스웨거 바로가기</h1>
                </a>
                <a href="./api/auth/kakao" target="_blank">
                    <h1>🍋 카카오 로그인 임시 테스트 - 프론트 리다이렉트</h1>
                </a>
                <a href="./api/auth/kakao/backend" target="_blank">
                    <h1>🍋 카카오 로그인 임시 테스트 - 백엔드 응답</h1>
                </a>
            </body>
            </html>
            """;
    }
}
