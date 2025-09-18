package com.tetz.spring_proj.exchange.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tetz.spring_proj.exchange.dto.ExchangeRateDTO;
import lombok.RequiredArgsConstructor;
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
public class ExchangeService {
    @Value("${exchange.api.key}")
    private String API_KEY;
    @Value("${exchange.api.base.url}")
    private String BASE_URL;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;

    private static final Map<String, CurrencyInfo> CURRENCY_CODES = Map.of(
            "USD", new CurrencyInfo("0000001", "미국 달러"),
            "JPY", new CurrencyInfo("0000002", "일본 엔"),
            "EUR", new CurrencyInfo("0000003", "유로"),
            "GBP", new CurrencyInfo("0000012", "영국 파운드"),
            "AUD", new CurrencyInfo("0000017", "호주 달러"),
            "CNY", new CurrencyInfo("0000053", "중국 위안")
    );

    // 통화 정보를 담는 내부 클래스
    private static class CurrencyInfo {
        final String code;
        final String name;

        CurrencyInfo(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    public ExchangeService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    // ## 동기 메서드

    // 단일 환율 조회
    public String getExchangeRateSync(String currencyCode) {
        CurrencyInfo currencyInfo = CURRENCY_CODES.get(currencyCode.toUpperCase());
        if (currencyInfo == null) {
            throw new IllegalArgumentException("Unsupported currency code: " + currencyCode);
        }

        // 주말인 경우 같은 주 금요일로 조정
        LocalDate requestDate = getBusinessDate(LocalDate.now());
        String dateString = requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String url = String.format("%s%s/json/kr/1/100/731Y001/D/%s/%s/%s",
                BASE_URL, API_KEY, dateString, dateString, currencyInfo.code);

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

    // 모든 환율 조회 (동기) - DTO 반환
    public ExchangeRateDTO getAllExchangeRatesSync() {
        LocalDate requestDate = getBusinessDate(LocalDate.now());
        String dateString = requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        List<ExchangeRateDTO.CurrencyRate> currencyRates = new ArrayList<>();
        boolean overallSuccess = true;
        StringBuilder errorMessages = new StringBuilder();

        for (Map.Entry<String, CurrencyInfo> entry : CURRENCY_CODES.entrySet()) {
            String currencyCode = entry.getKey();
            CurrencyInfo currencyInfo = entry.getValue();

            try {
                String url = String.format("%s%s/json/kr/1/100/731Y001/D/%s/%s/%s",
                        BASE_URL, API_KEY, dateString, dateString, currencyInfo.code);

                String jsonResponse = restTemplate.getForObject(url, String.class);
                if (jsonResponse != null) {
                    String exchangeRate = extractDataValue(jsonResponse);
                    currencyRates.add(ExchangeRateDTO.CurrencyRate.builder()
                            .currencyCode(currencyCode)
                            .currencyName(currencyInfo.name)
                            .exchangeRate(exchangeRate)
                            .isSuccess(true)
                            .build());
                    log.info("{}({}) 환율 조회 완료: {}", currencyCode, currencyInfo.code, exchangeRate);
                } else {
                    handleFailedCurrency(currencyRates, currencyCode, currencyInfo.name, "API 응답이 null입니다.");
                    overallSuccess = false;
                }
            } catch (Exception e) {
                log.error("{} 환율 조회 실패: {}", currencyCode, e.getMessage());
                handleFailedCurrency(currencyRates, currencyCode, currencyInfo.name, e.getMessage());
                overallSuccess = false;
                if (errorMessages.length() > 0) errorMessages.append(", ");
                errorMessages.append(currencyCode).append(" 조회 실패");
            }
        }

        return ExchangeRateDTO.builder()
                .requestDate(dateString)
                .success(overallSuccess)
                .message(overallSuccess ? "모든 환율 조회 성공" : "일부 환율 조회 실패: " + errorMessages.toString())
                .rates(currencyRates)
                .build();
    }

    // ## 비동기 메서드

    // 모든 환율을 비동기로 요청하는 메서드 - DTO 반환
    public ExchangeRateDTO getAllExchangeRatesAsync() {
        Map<String, CompletableFuture<ExchangeRateDTO.CurrencyRate>> futures = new HashMap<>();
        LocalDate requestDate = getBusinessDate(LocalDate.now());
        String dateString = requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 각 통화에 대해 비동기 요청
        for (Map.Entry<String, CurrencyInfo> entry : CURRENCY_CODES.entrySet()) {
            String currencyCode = entry.getKey();
            CurrencyInfo currencyInfo = entry.getValue();

            CompletableFuture<ExchangeRateDTO.CurrencyRate> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String url = String.format("%s%s/json/kr/1/100/731Y001/D/%s/%s/%s",
                            BASE_URL, API_KEY, dateString, dateString, currencyInfo.code);

                    String jsonResponse = restTemplate.getForObject(url, String.class);
                    if (jsonResponse != null) {
                        String exchangeRate = extractDataValue(jsonResponse);
                        log.info("{}({}) 환율 조회 완료: {}", currencyCode, currencyInfo.code, exchangeRate);
                        return ExchangeRateDTO.CurrencyRate.builder()
                                .currencyCode(currencyCode)
                                .currencyName(currencyInfo.name)
                                .exchangeRate(exchangeRate)
                                .isSuccess(true)
                                .build();
                    }
                    return createFailedCurrencyRate(currencyCode, currencyInfo.name, "API 응답이 null입니다.");
                } catch (Exception e) {
                    log.error("{} 환율 조회 실패: {}", currencyCode, e.getMessage());
                    return createFailedCurrencyRate(currencyCode, currencyInfo.name, e.getMessage());
                }
            }, executorService);

            futures.put(currencyCode, future);
        }

        // 비동기 작업 대기 및 결과 수집
        List<ExchangeRateDTO.CurrencyRate> currencyRates = new ArrayList<>();
        boolean overallSuccess = true;
        StringBuilder errorMessages = new StringBuilder();

        for (Map.Entry<String, CompletableFuture<ExchangeRateDTO.CurrencyRate>> entry : futures.entrySet()) {
            try {
                ExchangeRateDTO.CurrencyRate rate = entry.getValue().get();
                currencyRates.add(rate);
                if (!rate.isSuccess()) {
                    overallSuccess = false;
                    if (errorMessages.length() > 0) errorMessages.append(", ");
                    errorMessages.append(rate.getCurrencyCode()).append(" 조회 실패");
                }
            } catch (Exception e) {
                log.error("{} 환율 결과 수집 실패: {}", entry.getKey(), e.getMessage());
                CurrencyInfo currencyInfo = CURRENCY_CODES.get(entry.getKey());
                currencyRates.add(createFailedCurrencyRate(entry.getKey(),
                        currencyInfo != null ? currencyInfo.name : "Unknown", e.getMessage()));
                overallSuccess = false;
                if (errorMessages.length() > 0) errorMessages.append(", ");
                errorMessages.append(entry.getKey()).append(" 결과 수집 실패");
            }
        }

        return ExchangeRateDTO.builder()
                .requestDate(dateString)
                .success(overallSuccess)
                .message(overallSuccess ? "모든 환율 조회 성공" : "일부 환율 조회 실패: " + errorMessages.toString())
                .rates(currencyRates)
                .build();
    }

    // ## 공통 메서드 영역

    // 실패한 통화 정보를 currencyRates 리스트에 추가하는 헬퍼 메서드
    private void handleFailedCurrency(List<ExchangeRateDTO.CurrencyRate> currencyRates,
                                      String currencyCode, String currencyName, String errorMessage) {
        currencyRates.add(createFailedCurrencyRate(currencyCode, currencyName, errorMessage));
    }

    // 실패한 CurrencyRate 객체를 생성하는 헬퍼 메서드
    private ExchangeRateDTO.CurrencyRate createFailedCurrencyRate(String currencyCode,
                                                                  String currencyName, String errorMessage) {
        return ExchangeRateDTO.CurrencyRate.builder()
                .currencyCode(currencyCode)
                .currencyName(currencyName)
                .exchangeRate("N/A")
                .isSuccess(false)
                .errorMessage(errorMessage)
                .build();
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