package com.tetz.spring_proj.exchange.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateDTO {
    private Map<String, String> data;
    private long executionTimeMs;
}