package com.tetz.spring_proj.exchange.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exchange")
public class ExchangeController {
    @GetMapping("/rate")
    public ResponseEntity<Float> getExchangeRate(
            @RequestParam("country") String country) {

        String currencyCode = country.toUpperCase();



        return ResponseEntity.ok(1000F);
    }
}
