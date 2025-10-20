package com.tetz.spring_proj.ai.controller;

import com.tetz.spring_proj.ai.service.AiService;
import com.tetz.spring_proj.ai.service.AiService.AiProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Operation(summary = "AI 비교 및 종합",
            description = "두 모델(GPT, CLAUDE)에 같은 메시지를 전달하고 해당 응답의 공통점만을 스트리밍 (할루시네이션 방지용)")
    @GetMapping("/stream/compare")
    @PreAuthorize("permitAll()")
    public SseEmitter streamComparedAiResponse(@RequestParam String message) {

        log.info("AI 비교 스트리밍 요청 수신. Message: {}", message);

        return aiService.streamComparedAiResponse(message);
    }
}