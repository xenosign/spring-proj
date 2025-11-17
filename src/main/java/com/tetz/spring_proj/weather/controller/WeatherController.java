package com.tetz.spring_proj.weather.controller;

import com.tetz.spring_proj.weather.dto.WeatherResponseDto;
import com.tetz.spring_proj.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/weather")
@Tag(name = "날씨 조회 API", description = "도시명 또는 좌표 기반 날씨 정보 조회")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Operation(summary = "도시명으로 날씨 조회", description = "도시명과 국가 코드(선택)를 사용하여 날씨 정보 조회")
    @GetMapping
    public ResponseEntity<WeatherResponseDto> getWeather(
            @Parameter(description = "도시명 (예: Seoul)", required = true)
            @RequestParam String city,
            @Parameter(description = "국가 코드 (예: KR)")
            @RequestParam(required = false) String country) {
        log.info("날씨 조회 요청 - 도시명 {}, 국가명 {}", city, country);
        WeatherResponseDto result = weatherService.getWeatherAsync(city, country).block();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "좌표로 날씨 조회", description = "위도(latitude)와 경도(longitude)를 사용하여 날씨 정보 조회")
    @GetMapping("/coordinates")
    public ResponseEntity<WeatherResponseDto> getWeatherByCoordinates(
            @Parameter(description = "위도 (예: 37.5665)", required = true)
            @RequestParam Double lat,
            @Parameter(description = "경도 (예: 126.9780)", required = true)
            @RequestParam Double lon) {
        log.info("날씨 조회 요청 - 위도: {}, 경도: {}", lat, lon);
        WeatherResponseDto result = weatherService.getWeatherByCoordinatesAsync(lat, lon).block();
        return ResponseEntity.ok(result);
    }
}