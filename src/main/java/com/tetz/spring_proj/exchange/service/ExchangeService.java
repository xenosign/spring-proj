package com.tetz.spring_proj.exchange.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class ExchangeService {
    @Value("${exchange.api.key}")
    private String API_KEY;
    @Value("${exchange.api.base.url}")
    private String BASE_URL;
    private final RestTemplate restTemplate;


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

        // 주말인 경우 같은 주 금요일로 조정
        LocalDate requestDate = getBusinessDate(LocalDate.now());
        String dateString = requestDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String url = String.format("%s%s/json/kr/1/100/731Y001/D/%s/%s/%s",
                BASE_URL, API_KEY, dateString, dateString, upperCurrencyCode);


        // RestTemplate을 사용한 동기적 API 호출
        return restTemplate.getForObject(url, String.class);
    }

    // 주말에는 환율 업데이트가 안되므로 토 ~ 월요일 오전 9시 이전 까지는 금요일 기준으로 요청
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