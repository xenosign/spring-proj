package com.tetz.spring_proj.exchange.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetz.spring_proj.exchange.dto.ExchangeRateDTO;
import com.tetz.spring_proj.exchange.dto.ExchangeRateResponseDTO;
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
        this.executorService = Executors.newFixedThreadPool(10);
    }

    // ## 동기 메서드

    // 단일 환율 조회
    public String getExchangeRateSync(String currencyCode) {
        String upperCurrencyCode = CURRENCY_CODES.get(currencyCode.toUpperCase());
        if (upperCurrencyCode == null) {
            throw new IllegalArgumentException("Unsupported currency code: " + currencyCode);
        }

        // 주말인 경우 같은 주 금요일로 조정
        LocalDate requestDate = getBusinessDate(LocalDate.now());
        String dateString = requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String url = String.format("%s%s/json/kr/1/100/731Y001/D/%s/%s/%s",
                BASE_URL, API_KEY, dateString, dateString, upperCurrencyCode);

        try {
            // RestTemplate을 사용한 동기적 API 호출
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null) {
                throw new RuntimeException("API 응답이 null입니다.");
            }

            // JSON에서 DATA_VALUE 추출
            return extractDataValue(jsonResponse);

        } catch (Exception e) {
            log.error("환율 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("환율 조회 실패", e);
        }
    }

    // 특정 통화들의 환율 조회 (동기)
    public ExchangeRateResponseDTO getSpecificExchangeRatesSync(List<String> currencyCodes) {
        long startTime = System.currentTimeMillis();
        validateCurrencyCodes(currencyCodes);

        List<ExchangeRateDTO> exchangeRateDTOS = new ArrayList<>();
        LocalDate requestDate = getBusinessDate(LocalDate.now());
        String dateString = requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        for (String currencyCode : currencyCodes) {
            String upperCurrencyCode = currencyCode.toUpperCase();
            String currencyId = CURRENCY_CODES.get(upperCurrencyCode);

            try {
                String url = String.format("%s%s/json/kr/1/100/731Y001/D/%s/%s/%s",
                        BASE_URL, API_KEY, dateString, dateString, currencyId);

                String jsonResponse = restTemplate.getForObject(url, String.class);
                if (jsonResponse != null) {
                    String rate = extractDataValue(jsonResponse);
                    // [수정] ExchangeRate 객체를 생성하여 리스트에 추가
                    ExchangeRateDTO er = new ExchangeRateDTO();
                    er.setCurrency(upperCurrencyCode);
                    er.setRate(rate);
                    exchangeRateDTOS.add(er);
                    log.info("{}({}) 환율 조회 완료: {}", upperCurrencyCode, currencyId, rate);
                }
            } catch (Exception e) {
                log.error("{} 환율 조회 실패: {}", upperCurrencyCode, e.getMessage());
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;

        return ExchangeRateResponseDTO.builder()
                .rates(exchangeRateDTOS)
                .executionTimeMs(executionTime)
                .build();
    }

    // 모든 환율 조회 (동기)
    public ExchangeRateResponseDTO getAllExchangeRatesSync() {
        return getSpecificExchangeRatesSync(new ArrayList<>(CURRENCY_CODES.keySet()));
    }


    // ## 비동기 메서드

    // 특정 통화들의 환율 조회 (비동기)
    public ExchangeRateResponseDTO getSpecificExchangeRatesAsync(List<String> currencyCodes) {
        long startTime = System.currentTimeMillis();
        validateCurrencyCodes(currencyCodes);

        Map<String, CompletableFuture<String>> futures = new HashMap<>();
        LocalDate requestDate = getBusinessDate(LocalDate.now());
        String dateString = requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        for (String currencyCode : currencyCodes) {
            String upperCurrencyCode = currencyCode.toUpperCase();
            String currencyId = CURRENCY_CODES.get(upperCurrencyCode);

            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String url = String.format("%s%s/json/kr/1/100/731Y001/D/%s/%s/%s",
                            BASE_URL, API_KEY, dateString, dateString, currencyId);

                    String jsonResponse = restTemplate.getForObject(url, String.class);
                    if (jsonResponse != null) {
                        String exchangeRate = extractDataValue(jsonResponse);
                        log.info("{}({}) 환율 조회 완료: {}", upperCurrencyCode, currencyId, exchangeRate);
                        return exchangeRate;
                    }
                    return null;
                } catch (Exception e) {
                    log.error("{} 환율 조회 실패: {}", upperCurrencyCode, e.getMessage());
                    return null;
                }
            }, executorService);

            futures.put(upperCurrencyCode, future);
        }

        List<ExchangeRateDTO> exchangeRateDTOS = new ArrayList<>();
        for (Map.Entry<String, CompletableFuture<String>> entry : futures.entrySet()) {
            try {
                String rate = entry.getValue().get();
                if (rate != null) {
                    ExchangeRateDTO er = new ExchangeRateDTO();
                    er.setCurrency(entry.getKey());
                    er.setRate(rate);
                    exchangeRateDTOS.add(er);
                }
            } catch (Exception e) {
                log.error("{} 환율 결과 수집 실패: {}", entry.getKey(), e.getMessage());
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;

        return ExchangeRateResponseDTO.builder()
                .rates(exchangeRateDTOS)
                .executionTimeMs(executionTime)
                .build();
    }

    // 모든 환율을 비동기로 요청하는 메서드
    public ExchangeRateResponseDTO getAllExchangeRatesAsync() {
        return getSpecificExchangeRatesAsync(new ArrayList<>(CURRENCY_CODES.keySet()));
    }

    // ## 공통 메서드 영역

    // 통화 코드 검증
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

    // ECOS 응답 JSON 에서 환율 추출 메서드
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

    // 주말에는 환율 업데이트가 안되므로 토 ~ 월요일 오전 9시 이전 까지는 금요일로 날짜를 변경하는 메서드
    private LocalDate getBusinessDate(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        LocalTime currentTime = LocalTime.now();

        if (dayOfWeek == DayOfWeek.SATURDAY) {
            // 토요일인 케이스
            return date.minusDays(1);
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            // 일요일인 케이스
            return date.minusDays(2);
        } else if (dayOfWeek == DayOfWeek.MONDAY && currentTime.isBefore(LocalTime.of(9, 0))) {
            // 월요일 오전 9시 이전인 케이스
            return date.minusDays(3);
        }

        // 월요일 9시 이후 평일
        return date;
    }
}