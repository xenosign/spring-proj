package com.tetz.spring_proj.ai.controller;

import com.tetz.spring_proj.ai.service.AiService;
import com.tetz.spring_proj.ai.service.AiService.AiProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {
    private final AiService aiService;

    @Operation(summary = "AI SSE스트리밍",
            description = "AI 모델에 메시지를 전달하고 SSE 형식으로 응답을 스트리밍 (GPT, CLAUDE 완료 / GEMINI 작업 중)")
    @GetMapping("/stream")
    public SseEmitter streamAiResponse(
            @RequestParam String message,
            @RequestParam(defaultValue = "GPT") AiProvider provider) {

        log.info("AI 스트리밍 요청 수신. Provider: {}, Message: {}", provider, message);

        return aiService.streamAiResponse(message, provider);
    }
}