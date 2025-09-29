package com.tetz.spring_proj.exchange.dto;

import lombok.Data;

@Data
public class ExchangeRateDTO {
    private String currency;
    private String rate;
}
