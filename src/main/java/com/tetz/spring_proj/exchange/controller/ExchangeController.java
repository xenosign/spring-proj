package com.tetz.spring_proj.exchange.controller;

import com.tetz.spring_proj.exchange.dto.ExchangeRateDTO;
import com.tetz.spring_proj.exchange.service.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@Tag(name = "환율 조회 API", description = "국가별 환율 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exchange")
public class ExchangeController {
    private final ExchangeService exchangeService;

    @Operation(summary = "단일 국가 환율 조회 (동기)", description = "국가명(USD, JPY, EUR, GBP, AUD, CNY / 소문자 가능)을 사용하여 환율을 조회. 유효하지 않을 경우 400")
    @GetMapping("/rate")
    public ResponseEntity<String> getExchangeRate(
            @Parameter(description = "조회할 국가명 (예: USD)", required = true)
            @RequestParam("country") String country) {
        try {
            String exchangeRate = exchangeService.getExchangeRateSync(country);
            return ResponseEntity.ok(exchangeRate);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "전체 국가 환율 조회 (동기)",
            description = "지원하는 모든 국가(USD, JPY, EUR, GBP, AUD, CNY)의 환율을 순서대로 동기적으로 조회")
    @GetMapping("/rate/all/sync")
    public ResponseEntity<ExchangeRateDTO> getAllExchangeRatesSync() {
        try {
            ExchangeRateDTO exchangeRates = exchangeService.getAllExchangeRatesSync();
            return ResponseEntity.ok(exchangeRates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "전체 국가 환율 조회 (비동기)",
            description = "지원하는 모든 국가(USD, JPY, EUR, GBP, AUD, CNY)의 환율을 병렬로 비동기적으로 조회")
    @GetMapping("/rate/all/async")
    public ResponseEntity<ExchangeRateDTO> getAllExchangeRatesAsync() {
        try {
            ExchangeRateDTO exchangeRates = exchangeService.getAllExchangeRatesAsync();
            return ResponseEntity.ok(exchangeRates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
