package com.tetz.spring_proj.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Value("${ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${ai.gemini.api-key}")
    private String geminiApiKey;

    @Value("${ai.claude.api-key}")
    private String claudeApiKey;

    public enum AiProvider {
        GPT, CLAUDE, GEMINI
    }

    // 각각 AI 서비스(GPT, CLAUDE, GEMINI) 응답 요청
    public SseEmitter streamAiResponse(String userMessage, AiProvider provider) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        executor.execute(() -> {
            try {
                switch (provider) {
                    case GPT -> streamGptResponse(emitter, userMessage);
                    case CLAUDE -> streamClaudeResponse(emitter, userMessage);
                    case GEMINI -> streamGeminiResponse(emitter, userMessage);
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
            String[] lines = chunk.split("\n");

            for (String line : lines) {
                line = line.trim();

                if (line.isEmpty()) continue;

                if ("[DONE]".equals(line) || "data: [DONE]".equals(line)) {
                    log.info("GPT 스트림 완료 신호");
                    continue;
                }

                String jsonData = line;
                if (line.startsWith("data: ")) {
                    jsonData = line.substring(6).trim();
                    if ("[DONE]".equals(jsonData)) continue;
                }

                try {
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
                } catch (Exception e) {
                    log.warn("JSON 파싱 실패 (무시): {}", jsonData);
                }
            }

        } catch (Exception e) {
            log.error("GPT 청크 처리 중 오류", e);
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
                .doOnError(error -> log.error("Claude Flux 에러", error))
                .subscribe(
                        chunk -> handleClaudeChunk(emitter, chunk),
                        error -> handleError(emitter, error),
                        () -> handleComplete(emitter)
                );
    }

    private Map<String, Object> createClaudeRequestBody(String userMessage) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-5-sonnet-20241022");
        requestBody.put("max_tokens", 1024);
        requestBody.put("stream", true);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", userMessage);

        requestBody.put("messages", List.of(message));

        return requestBody;
    }

    private void handleClaudeChunk(SseEmitter emitter, String chunk) {
        try {
            chunk = chunk.trim();

            if (chunk.isEmpty()) return;

            try {
                JsonNode rootNode = objectMapper.readTree(chunk);
                String type = rootNode.path("type").asText("");

                if ("content_block_delta".equals(type)) {
                    JsonNode delta = rootNode.path("delta");
                    String deltaType = delta.path("type").asText("");

                    if ("text_delta".equals(deltaType)) {
                        String text = delta.path("text").asText("");

                        if (!text.isEmpty()) {
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(text));
                        }
                    }
                } else if ("ping".equals(type)) {
                    log.debug("Claude ping");
                }
            } catch (Exception e) {
                log.warn("Claude JSON 파싱 실패: {}", chunk, e);
            }

        } catch (Exception e) {
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
            emitter.completeWithError(e);
        }
        emitter.completeWithError(error);
    }

    private void handleComplete(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("done")
                    .data("완료"));
            emitter.complete();
        } catch (Exception e) {
            log.error("완료 이벤트 전송 실패 또는 Emitter 닫기 실패", e);
            emitter.completeWithError(e);
        }
    }

    // ========== GEMINI (SSE) ==========
    private void streamGeminiResponse(SseEmitter emitter, String userMessage) {
        Map<String, Object> requestBody = createGeminiRequestBody(userMessage);

        String uri = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:streamGenerateContent?key=%s&alt=sse",
                geminiApiKey
        );

        webClient.post()
                .uri(uri)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(error -> log.error("Gemini Flux 에러", error))
                .subscribe(
                        chunk -> handleGeminiChunk(emitter, chunk),
                        error -> handleError(emitter, error),
                        () -> handleComplete(emitter)
                );
    }

    private Map<String, Object> createGeminiRequestBody(String userMessage) {
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> part = new HashMap<>();
        part.put("text", userMessage);

        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        content.put("parts", List.of(part));

        requestBody.put("contents", List.of(content));

        return requestBody;
    }

    private void handleGeminiChunk(SseEmitter emitter, String chunk) {
        try {
            String[] lines = chunk.split("\n");

            for (String line : lines) {
                line = line.trim();

                if (line.isEmpty()) continue;

                String jsonData = line;
                if (line.startsWith("data: ")) {
                    jsonData = line.substring(6).trim();
                }

                if (jsonData.isEmpty()) continue;

                try {
                    JsonNode rootNode = objectMapper.readTree(jsonData);
                    JsonNode candidates = rootNode.path("candidates");

                    if (candidates.isArray() && candidates.size() > 0) {
                        JsonNode candidate = candidates.get(0);
                        JsonNode content = candidate.path("content");
                        JsonNode parts = content.path("parts");

                        if (parts.isArray() && parts.size() > 0) {
                            JsonNode part = parts.get(0);
                            String text = part.path("text").asText("");

                            if (!text.isEmpty()) {
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(text));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Gemini JSON 파싱 실패: {}", jsonData, e);
                }
            }

        } catch (Exception e) {
            log.error("Gemini 청크 처리 중 오류", e);
            emitter.completeWithError(e);
        }
    }

    @jakarta.annotation.PreDestroy
    public void shutdownExecutor() {
        executor.shutdown();
    }

    // 각각 AI 서비스(GPT, CLAUDE) 응답 중 공통 부분만 추출하여 응답
    public SseEmitter streamComparedAiResponse(String userMessage) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        executor.execute(() -> {
            try {
                StringBuilder gptResponse = new StringBuilder();
                StringBuilder claudeResponse = new StringBuilder();

                // 두 모델의 응답을 동시에 수집
                CompletableFuture<String> gptFuture = CompletableFuture.supplyAsync(() -> {
                    collectGptResponse(gptResponse, userMessage);
                    return gptResponse.toString();
                }, executor);

                CompletableFuture<String> claudeFuture = CompletableFuture.supplyAsync(() -> {
                    collectClaudeResponse(claudeResponse, userMessage);
                    return claudeResponse.toString();
                }, executor);

                // 두 응답이 모두 완료될 때까지 대기
                CompletableFuture.allOf(gptFuture, claudeFuture).join();

                String gptText = gptFuture.get();
                String claudeText = claudeFuture.get();

                log.info("GPT 응답 수집 완료: {} chars", gptText.length());
                log.info("Claude 응답 수집 완료: {} chars", claudeText.length());

                streamCommonPoints(emitter, gptText, claudeText, userMessage);

            } catch (Exception e) {
                log.error("AI 비교 스트리밍 중 오류 발생", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private void collectGptResponse(StringBuilder result, String userMessage) {
        Map<String, Object> requestBody = createGptRequestBody(userMessage);

        try {
            webClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .doOnNext(chunk -> {
                        String content = extractGptContent(chunk);
                        if (!content.isEmpty()) {
                            result.append(content);
                        }
                    })
                    .blockLast(); // 모든 청크 수집 완료 대기
        } catch (Exception e) {
            log.error("GPT 응답 수집 중 오류", e);
        }
    }

    private String extractGptContent(String chunk) {
        try {
            String[] lines = chunk.split("\n");
            StringBuilder content = new StringBuilder();

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || "[DONE]".equals(line) || "data: [DONE]".equals(line)) {
                    continue;
                }

                String jsonData = line.startsWith("data: ") ? line.substring(6).trim() : line;
                if ("[DONE]".equals(jsonData)) continue;

                try {
                    JsonNode rootNode = objectMapper.readTree(jsonData);
                    JsonNode choices = rootNode.path("choices");

                    if (choices.isArray() && choices.size() > 0) {
                        JsonNode delta = choices.get(0).path("delta");
                        String text = delta.path("content").asText("");
                        content.append(text);
                    }
                } catch (Exception e) {
                    log.debug("GPT JSON 파싱 스킵: {}", jsonData);
                }
            }

            return content.toString();
        } catch (Exception e) {
            log.error("GPT 컨텐츠 추출 중 오류", e);
            return "";
        }
    }

    private void collectClaudeResponse(StringBuilder result, String userMessage) {
        Map<String, Object> requestBody = createClaudeRequestBody(userMessage);

        try {
            webClient.post()
                    .uri("https://api.anthropic.com/v1/messages")
                    .header("x-api-key", claudeApiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .doOnNext(chunk -> {
                        String content = extractClaudeContent(chunk);
                        if (!content.isEmpty()) {
                            result.append(content);
                        }
                    })
                    .blockLast(); // 모든 청크 수집 완료 대기
        } catch (Exception e) {
            log.error("Claude 응답 수집 중 오류", e);
        }
    }

    private String extractClaudeContent(String chunk) {
        try {
            chunk = chunk.trim();
            if (chunk.isEmpty()) return "";

            JsonNode rootNode = objectMapper.readTree(chunk);
            String type = rootNode.path("type").asText("");

            if ("content_block_delta".equals(type)) {
                JsonNode delta = rootNode.path("delta");
                String deltaType = delta.path("type").asText("");

                if ("text_delta".equals(deltaType)) {
                    return delta.path("text").asText("");
                }
            }

            return "";
        } catch (Exception e) {
            log.debug("Claude JSON 파싱 스킵: {}", chunk);
            return "";
        }
    }

    private void streamCommonPoints(SseEmitter emitter, String gptText, String claudeText, String originalMessage) {
        try {
            String analysisPrompt = String.format("""
        다음은 같은 질문에 대한 두 AI 모델의 응답입니다.
        
        질문: %s
        
        [GPT 응답]
        %s
        
        [Claude 응답]
        %s
        
        두 응답의 공통된 핵심 내용만을 추출하여 간결하게 정리해주세요.
        서로 다른 내용이나 모순되는 부분은 제외하고, 두 모델이 모두 동의하는 사실과 정보만 포함하세요.
        """, originalMessage, gptText, claudeText);

            Map<String, Object> requestBody = createGptRequestBody(analysisPrompt);

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

        } catch (Exception e) {
            log.error("공통점 추출 중 오류", e);
            emitter.completeWithError(e);
        }
    }
}