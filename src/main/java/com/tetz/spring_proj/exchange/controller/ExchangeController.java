package com.tetz.spring_proj.exchange.controller;

import com.tetz.spring_proj.exchange.dto.ExchangeRateResponseDto;
import com.tetz.spring_proj.exchange.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "환율 조회 API", description = "국가별 환율 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exchange")
public class ExchangeController {
    private final ExchangeRateService exchangeRateService;

    @Operation(summary = "단일 국가 환율 조회 (동기)", description = "국가명(USD, JPY, EUR, GBP, AUD, CNY / 소문자 가능)을 사용하여 환율을 조회. 유효하지 않을 경우 400")
    @GetMapping("/rate")
    public ResponseEntity<String> getExchangeRate(
            @Parameter(description = "조회할 국가명 (예: USD)", required = true)
            @RequestParam("country") String country) {
        try {
            String exchangeRate = exchangeRateService.getExchangeRateSync(country);
            return ResponseEntity.ok(exchangeRate);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "전체 국가 환율 조회 (동기)",
            description = "지원하는 모든 국가(USD, JPY, EUR, GBP, AUD, CNY)의 환율을 순서대로 동기적으로 조회")
    @GetMapping("/rate/all/sync")
    public ResponseEntity<ExchangeRateResponseDto> getAllExchangeRatesSync() {
        try {
            ExchangeRateResponseDto exchangeRates = exchangeRateService.getAllExchangeRatesSync();
            return ResponseEntity.ok(exchangeRates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "전체 국가 환율 조회 (비동기)",
            description = "지원하는 모든 국가(USD, JPY, EUR, GBP, AUD, CNY)의 환율을 병렬로 비동기적으로 조회")
    @GetMapping("/rate/all/async")
    public ResponseEntity<ExchangeRateResponseDto> getAllExchangeRatesAsync() {
        try {
            ExchangeRateResponseDto exchangeRates = exchangeRateService.getAllExchangeRatesAsync();
            return ResponseEntity.ok(exchangeRates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "특정 국가들 환율 조회 (동기)",
            description = "쿼리 파라미터로 지정한 국가들의 환율을 순서대로 동기적으로 조회. ex) /rates/sync?currencies=USD,JPY,CNY")
    @GetMapping("/rate/sync")
    public ResponseEntity<?> getSpecificExchangeRatesSync(
            @Parameter(description = "조회할 국가명 리스트 (쉼표로 구분, 예: USD,JPY,EUR,GBP,AUD,CNY)", required = true)
            @RequestParam("currencies") String currencies) {
        try {
            // 쉼표로 구분된 문자열을 리스트로 변환
            List<String> currencyList = List.of(currencies.split(","));

            // 각 통화 코드의 공백 제거 및 대문자 변환
            currencyList = currencyList.stream()
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .toList();

            ExchangeRateResponseDto result = exchangeRateService.getSpecificExchangeRatesSync(currencyList);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("환율 조회 중 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "특정 국가들 환율 조회 (비동기)",
            description = "쿼리 파라미터로 지정한 국가들의 환율을 병렬로 비동기적으로 조회. ex) /rates/async?currencies=USD,JPY,CNY")
    @GetMapping("/rate/async")
    public ResponseEntity<?> getSpecificExchangeRatesAsync(
            @Parameter(description = "조회할 국가명 리스트 (쉼표로 구분, 예: USD,JPY,EUR,GBP,AUD,CNY)", required = true)
            @RequestParam("currencies") String currencies) {
        try {
            // 쉼표로 구분된 문자열을 리스트로 변환
            List<String> currencyList = List.of(currencies.split(","));

            // 각 통화 코드의 공백 제거 및 대문자 변환
            currencyList = currencyList.stream()
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .toList();

            ExchangeRateResponseDto result = exchangeRateService.getSpecificExchangeRatesAsync(currencyList);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("환율 조회 중 오류가 발생했습니다.");
        }
    }
}
