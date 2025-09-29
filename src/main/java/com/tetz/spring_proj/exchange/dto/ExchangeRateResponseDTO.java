package com.tetz.spring_proj.exchange.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateResponseDTO {
    private List<ExchangeRateDTO> rates;
    private long executionTimeMs;
}