package com.tetz.spring_proj.ai.controller;

import com.tetz.spring_proj.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {
    private final AiService aiService;

    @GetMapping
    public String hello() {
        aiService.streamAiResponse("hello", AiService.AiProvider.GEMINI);
        return "Hello My City";
    }
}
