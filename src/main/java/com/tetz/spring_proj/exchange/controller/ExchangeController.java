package com.tetz.spring_proj.exchange.controller;

import com.tetz.spring_proj.exchange.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exchange")
public class ExchangeController {
    private final ExchangeService exchangeService;

    @GetMapping("/rate")
    public ResponseEntity<String> getExchangeRate(
            @RequestParam("country") String country) {

        String currencyCode = country.toUpperCase();

        String exchangeRate = exchangeService.getExchangeRateSync(currencyCode);

        return ResponseEntity.ok(exchangeRate);
    }
}
