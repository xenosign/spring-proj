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
        WeatherResponseDto result = weatherService.getWeatherAsync(city, country).block();

        return ResponseEntity.ok(result);
    }
}
