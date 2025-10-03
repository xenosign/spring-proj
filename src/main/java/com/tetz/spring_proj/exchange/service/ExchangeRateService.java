package com.tetz.spring_proj.exchange.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetz.spring_proj.exchange.dto.ExchangeRateDto;
import com.tetz.spring_proj.exchange.dto.ExchangeRateResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ExchangeRateService {
    @Value("${exchange.api.key}")
    private String API_KEY;
    @Value("${exchange.api.base.url}")
    private String BASE_URL;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;

    // 한국은행 ECOS API의 통계항목 코드는 고정
    private static final Map<String, String> CURRENCY_CODES = Map.of(
            "USD", "0000001",
            "JPY", "0000002",
            "EUR", "0000003",
            "GBP", "0000012",
            "AUD", "0000017",
            "CNY", "0000053"
    );

    public ExchangeRateService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        // API 호출을 위한 스레드 풀 설정
        this.executorService = Executors.newFixedThreadPool(10);
    }

    // ## 동기 메서드 (수정: 재시도 로직 적용)

    // 단일 환율 조회 (내부 재시도 로직 사용)
    public String getExchangeRateSync(String currencyCode) {
        String upperCurrencyCode = currencyCode.toUpperCase();
        String currencyId = CURRENCY_CODES.get(upperCurrencyCode);

        if (currencyId == null) {
            throw new IllegalArgumentException("Unsupported currency code: " + currencyCode);
        }

        try {
            // 최대 5일 전까지 거슬러 올라가며 환율 데이터를 조회합니다.
            String rate = fetchRateWithRetry(currencyId, LocalDate.now(), 5);
            if (rate == null) {
                throw new RuntimeException(upperCurrencyCode + " 환율 데이터를 찾을 수 없습니다.");
            }
            return rate;
        } catch (Exception e) {
            log.error("단일 환율 조회 실패: {}", e.getMessage());
            throw new RuntimeException("환율 조회 실패", e);
        }
    }

    // 특정 통화들의 환율 조회 (동기, 재시도 로직 적용)
    public ExchangeRateResponseDto getSpecificExchangeRatesSync(List<String> currencyCodes) {
        long startTime = System.currentTimeMillis();
        validateCurrencyCodes(currencyCodes);

        List<ExchangeRateDto> exchangeRateDtos = new ArrayList<>();

        for (String currencyCode : currencyCodes) {
            String upperCurrencyCode = currencyCode.toUpperCase();
            String currencyId = CURRENCY_CODES.get(upperCurrencyCode);

            try {
                // 재시도 로직이 적용된 메서드 호출
                String rate = fetchRateWithRetry(currencyId, LocalDate.now(), 5);

                if (rate != null) {
                    ExchangeRateDto er = new ExchangeRateDto();
                    er.setCurrency(upperCurrencyCode);
                    er.setRate(rate);
                    exchangeRateDtos.add(er);
                }
            } catch (Exception e) {
                log.error("{} 환율 조회 실패 (최종): {}", upperCurrencyCode, e.getMessage());
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;

        return ExchangeRateResponseDto.builder()
                .rates(exchangeRateDtos)
                .executionTimeMs(executionTime)
                .build();
    }

    // 모든 환율 조회 (동기)
    public ExchangeRateResponseDto getAllExchangeRatesSync() {
        return getSpecificExchangeRatesSync(new ArrayList<>(CURRENCY_CODES.keySet()));
    }

    // ## 비동기 메서드 (수정: 재시도 로직 적용)

    // 특정 통화들의 환율 조회 (비동기, 재시도 로직 적용)
    public ExchangeRateResponseDto getSpecificExchangeRatesAsync(List<String> currencyCodes) {
        long startTime = System.currentTimeMillis();
        validateCurrencyCodes(currencyCodes);

        List<CompletableFuture<ExchangeRateDto>> futures = new ArrayList<>();

        for (String currencyCode : currencyCodes) {
            String upperCurrencyCode = currencyCode.toUpperCase();
            String currencyId = CURRENCY_CODES.get(upperCurrencyCode);

            // CompletableFuture를 사용하여 비동기적으로 API 호출
            CompletableFuture<ExchangeRateDto> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // 재시도 로직이 적용된 메서드 호출
                    String rate = fetchRateWithRetry(currencyId, LocalDate.now(), 5);

                    if (rate != null) {
                        ExchangeRateDto er = new ExchangeRateDto();
                        er.setCurrency(upperCurrencyCode);
                        er.setRate(rate);
                        return er;
                    }
                } catch (Exception e) {
                    log.error("{} 환율 조회 실패 (비동기): {}", upperCurrencyCode, e.getMessage());
                }
                return null; // 실패 시 null 반환
            }, executorService);

            futures.add(future);
        }

        // 모든 비동기 작업의 완료를 기다립니다.
        List<ExchangeRateDto> exchangeRateDtos = futures.stream()
                .map(future -> {
                    try {
                        // 5초 타임아웃 설정
                        return future.get(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error("비동기 결과 수집 중 타임아웃 또는 예외 발생", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());

        long executionTime = System.currentTimeMillis() - startTime;

        return ExchangeRateResponseDto.builder()
                .rates(exchangeRateDtos)
                .executionTimeMs(executionTime)
                .build();
    }

    // 모든 환율을 비동기로 요청하는 메서드
    public ExchangeRateResponseDto getAllExchangeRatesAsync() {
        return getSpecificExchangeRatesAsync(new ArrayList<>(CURRENCY_CODES.keySet()));
    }


    // ## 공통 및 핵심 로직
    private String fetchRateWithRetry(String currencyId, LocalDate startDate, int maxRetries) {
        LocalDate currentDate = startDate;

        for (int i = 0; i < maxRetries; i++) {
            LocalDate requestDate = getRequestDate(currentDate);
            String dateString = requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            String url = String.format("%s%s/json/kr/1/100/731Y001/D/%s/%s/%s",
                    BASE_URL, API_KEY, dateString, dateString, currencyId);

            try {
                String jsonResponse = restTemplate.getForObject(url, String.class);

                if (jsonResponse != null && jsonResponse.contains("\"CODE\":\"INFO-200\"")) {
                    log.warn("ECOS API: {}에 데이터 없음. 이전 날짜({})로 재시도.", dateString, requestDate.minusDays(1));
                    currentDate = currentDate.minusDays(1); // 날짜 하루 전으로 이동
                    continue;
                }

                return extractDataValue(jsonResponse);

            } catch (Exception e) {
                log.error("API 요청 실패 (날짜: {}): {}", dateString, e.getMessage());
                break;
            }
        }
        return null;
    }

    private void validateCurrencyCodes(List<String> currencyCodes) {
        if (currencyCodes == null || currencyCodes.isEmpty()) {
            throw new IllegalArgumentException("통화 코드 리스트가 비어있습니다.");
        }

        List<String> unsupportedCodes = new ArrayList<>();
        for (String code : currencyCodes) {
            if (!CURRENCY_CODES.containsKey(code.toUpperCase())) {
                unsupportedCodes.add(code);
            }
        }

        if (!unsupportedCodes.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("지원하지 않는 통화 코드: %s. 지원 가능한 통화: %s",
                            unsupportedCodes, CURRENCY_CODES.keySet())
            );
        }
    }

    private String extractDataValue(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode statisticSearch = rootNode.get("StatisticSearch");

            if (statisticSearch == null) throw new RuntimeException("StatisticSearch 노드를 찾을 수 없습니다.");
            JsonNode rowArray = statisticSearch.get("row");

            if (rowArray == null || !rowArray.isArray() || rowArray.size() == 0) throw new RuntimeException("row 데이터를 찾을 수 없습니다.");
            JsonNode firstRow = rowArray.get(0);
            JsonNode dataValue = firstRow.get("DATA_VALUE");

            if (dataValue == null) throw new RuntimeException("DATA_VALUE를 찾을 수 없습니다.");
            return dataValue.asText();
        } catch (Exception e) {
            log.error("JSON 파싱 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    private LocalDate getRequestDate(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        LocalTime currentTime = LocalTime.now();

        if (dayOfWeek == DayOfWeek.MONDAY && currentTime.isBefore(LocalTime.of(9, 0))) {
            return date.minusDays(3);
        }

        else if (dayOfWeek.getValue() >= DayOfWeek.TUESDAY.getValue()
                && dayOfWeek.getValue() <= DayOfWeek.FRIDAY.getValue()
                && currentTime.isBefore(LocalTime.of(9, 0))) {
            return date.minusDays(1);
        }

        return date;
    }

    @jakarta.annotation.PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}