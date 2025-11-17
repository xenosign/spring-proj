package com.tetz.spring_proj.weather.controller;

import com.tetz.spring_proj.weather.dto.WeatherRequestDto;
import com.tetz.spring_proj.weather.dto.WeatherResponseDto;
import com.tetz.spring_proj.weather.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public ResponseEntity<WeatherResponseDto> getWeather(
            @RequestParam String city,
            @RequestParam(required = false) String country) {

        log.info("=== Weather API 호출 시작 ===");
        log.info("요청 파라미터 - city: {}, country: {}", city, country);

        WeatherResponseDto result = weatherService.getWeatherAsync(city, country)
                .doOnSuccess(response -> {
                    log.info("=== API 호출 성공 ===");
                    log.info("응답 데이터: {}", response);
                })
                .doOnError(error -> {
                    log.error("=== API 호출 실패 ===");
                    log.error("에러 메시지: {}", error.getMessage());
                })
                .block(); // Mono를 블로킹해서 실제 값 추출

        return ResponseEntity.ok(result);
    }
}
