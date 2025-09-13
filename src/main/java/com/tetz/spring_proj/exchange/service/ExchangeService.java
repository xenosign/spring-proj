package com.tetz.spring_proj.exchange.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ExchangeService {

    private final RestTemplate restTemplate;
    private final String API_KEY = "";
    private final String BASE_URL = "https://ecos.bok.or.kr/api/StatisticSearch/";

    private static final Map<String, String> CURRENCY_CODES = Map.of(
            "USD", "0000001",
            "JPY", "0000002",
            "EUR", "0000003",
            "GBP", "0000004",
            "AUD", "0000006",
            "CNY", "0000007"
    );

    public ExchangeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getExchangeRateSync(String currencyCode) {
        String upperCurrencyCode = CURRENCY_CODES.get(currencyCode.toUpperCase());
        if (upperCurrencyCode == null) {
            throw new IllegalArgumentException("Unsupported currency code: " + currencyCode);
        }

        String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        String url = String.format("%s%s/json/kr/1/100/731Y001/D/%s/%s/%s",
                BASE_URL, API_KEY, today, today, upperCurrencyCode);

        // RestTemplate을 사용한 동기적 API 호출
        return restTemplate.getForObject(url, String.class);
    }
}