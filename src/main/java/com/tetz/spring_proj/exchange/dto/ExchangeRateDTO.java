package com.tetz.spring_proj.exchange.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateDTO {
    private String requestDate;
    private boolean success;
    private String message;
    private List<CurrencyRate> rates;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrencyRate {
        private String currencyCode;
        private String currencyName;
        private String exchangeRate;
        private boolean isSuccess;
        private String errorMessage;
    }
}