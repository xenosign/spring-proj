package com.tetz.spring_proj.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    // @PostConstruct 대신 생성자에서 직접 초기화 또는 @Bean으로 관리하는 것이 좋습니다.
    // 여기서는 @PreDestroy를 위해 클래스 멤버로 유지합니다.
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Value("${ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${ai.gemini.api-key}")
    private String geminiApiKey;

    @Value("${ai.claude.api-key}")
    private String claudeApiKey;

    public enum AiProvider {
        GPT, GEMINI, CLAUDE
    }

    public SseEmitter streamAiResponse(String userMessage, AiProvider provider) {
        // SSE 연결이 닫히지 않도록 충분히 긴 타임아웃 설정
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // 비동기 처리를 위해 ExecutorService 사용
        executor.execute(() -> {
            try {
                switch (provider) {
                    case GPT -> streamGptResponse(emitter, userMessage);
                    case GEMINI -> streamGeminiResponse(emitter, userMessage);
                    case CLAUDE -> streamClaudeResponse(emitter, userMessage);
                }
            } catch (Exception e) {
                log.error("AI 스트리밍 중 오류 발생: {}", provider, e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    // ========== GPT (SSE) ==========
    private void streamGptResponse(SseEmitter emitter, String userMessage) {
        Map<String, Object> requestBody = createGptRequestBody(userMessage);

        webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe(
                        chunk -> handleGptChunk(emitter, chunk),
                        error -> handleError(emitter, error),
                        () -> handleComplete(emitter)
                );
    }

    private Map<String, Object> createGptRequestBody(String userMessage) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("stream", true);

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", userMessage);

        requestBody.put("messages", List.of(message));
        requestBody.put("max_tokens", 1000);
        requestBody.put("temperature", 0.7);

        return requestBody;
    }

    private void handleGptChunk(SseEmitter emitter, String chunk) {
        try {
            if (chunk.startsWith("data: ")) {
                String jsonData = chunk.substring(6).trim();

                if ("[DONE]".equals(jsonData)) return;

                JsonNode rootNode = objectMapper.readTree(jsonData);
                JsonNode choices = rootNode.path("choices");

                if (choices.isArray() && choices.size() > 0) {
                    JsonNode delta = choices.get(0).path("delta");
                    String content = delta.path("content").asText("");

                    if (!content.isEmpty()) {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(content));
                    }
                }
            }
        } catch (IOException e) {
            log.error("GPT 청크 처리 중 오류", e);
            emitter.completeWithError(e);
        }
    }

    // ========== GEMINI (JSON 배열 형식 스트림) ==========
    private void streamGeminiResponse(SseEmitter emitter, String userMessage) {
        Map<String, Object> requestBody = createGeminiRequestBody(userMessage);

        webClient.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:streamGenerateContent?key=" + geminiApiKey)
                .header("Content-Type", "application/json")
                // Gemini API는 스트림 응답이 JSON 배열 형태이므로,
                // String으로 전체를 받은 후 \n 기준으로 분리하는 커스텀 처리가 필요합니다.
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(chunk -> Flux.fromStream(Stream.of(chunk.split("\n")))) // 💡핵심: 줄바꿈 기준으로 분리
                .filter(s -> !s.trim().isEmpty())
                .subscribe(
                        chunk -> handleGeminiChunk(emitter, chunk),
                        error -> handleError(emitter, error),
                        () -> handleComplete(emitter)
                );
    }

    private Map<String, Object> createGeminiRequestBody(String userMessage) {
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, String> part = new HashMap<>();
        part.put("text", userMessage);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        requestBody.put("contents", List.of(content));

        return requestBody;
    }

    private void handleGeminiChunk(SseEmitter emitter, String chunk) {
        try {
            // chunk는 이제 줄바꿈으로 분리된, 하나의 완전한 JSON 객체입니다.
            JsonNode rootNode = objectMapper.readTree(chunk);
            JsonNode candidates = rootNode.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText("");

                    if (!text.isEmpty()) {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(text));
                    }
                }
            }
        } catch (IOException e) {
            log.error("Gemini 청크 처리 중 오류", e);
            emitter.completeWithError(e);
        }
    }

    // ========== CLAUDE (SSE) ==========
    private void streamClaudeResponse(SseEmitter emitter, String userMessage) {
        Map<String, Object> requestBody = createClaudeRequestBody(userMessage);

        webClient.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", claudeApiKey)
                .header("anthropic-version", "2023-06-01")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe(
                        chunk -> handleClaudeChunk(emitter, chunk),
                        error -> handleError(emitter, error),
                        () -> handleComplete(emitter)
                );
    }

    private Map<String, Object> createClaudeRequestBody(String userMessage) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-sonnet-20240229");
        requestBody.put("max_tokens", 1024);
        requestBody.put("stream", true);

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", userMessage);

        requestBody.put("messages", List.of(message));

        return requestBody;
    }

    private void handleClaudeChunk(SseEmitter emitter, String chunk) {
        try {
            if (chunk.startsWith("data: ")) {
                String jsonData = chunk.substring(6).trim();

                if ("[DONE]".equals(jsonData)) return;

                JsonNode rootNode = objectMapper.readTree(jsonData);
                String type = rootNode.path("type").asText("");

                if ("content_block_delta".equals(type)) {
                    JsonNode delta = rootNode.path("delta");
                    String text = delta.path("text").asText("");

                    if (!text.isEmpty()) {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(text));
                    }
                }
            }
        } catch (IOException e) {
            log.error("Claude 청크 처리 중 오류", e);
            emitter.completeWithError(e);
        }
    }

    // ========== 공통 처리 ==========
    private void handleError(SseEmitter emitter, Throwable error) {
        log.error("AI API 호출 중 오류 발생", error);
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("오류 발생: " + error.getMessage()));
        } catch (IOException e) {
            log.error("에러 전송 실패", e);
            emitter.completeWithError(e); // 💡 개선: 에러 전송 실패 시 최종 처리
        }
        emitter.completeWithError(error);
    }

    private void handleComplete(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("done")
                    .data("완료"));
            emitter.complete();
        } catch (IOException e) {
            log.error("완료 이벤트 전송 실패", e);
            emitter.completeWithError(e); // 💡 개선: 완료 이벤트 전송 실패 시 최종 처리
        }
    }

    @jakarta.annotation.PreDestroy
    public void shutdownExecutor() {
        executor.shutdown();
    }
}