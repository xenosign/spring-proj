package com.tetz.spring_proj.weather.controller;

import com.tetz.spring_proj.weather.dto.WeatherRequestDto;
import com.tetz.spring_proj.weather.dto.WeatherResponseDto;
import com.tetz.spring_proj.weather.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
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
    public Mono<WeatherResponseDto> getWeather(
            @RequestParam String city,
            @RequestParam(required = false) String country) {

        Mono<WeatherResponseDto> result = weatherService.getWeatherAsync(city, country);
        log.info("getWeather called {}", result);
        return result;
    }

    @GetMapping("/cities")
    public Flux<WeatherResponseDto> getWeatherForCities(
            @RequestParam WeatherRequestDto request) {
        return weatherService.getWeatherForCitiesAsync(request.getCities());
    }

    @GetMapping("/coordinates")
    public Mono<WeatherResponseDto> getWeatherByCoordinates(
            @RequestParam Double lat,
            @RequestParam Double lon) {
        return weatherService.getWeatherByCoordinatesAsync(lat, lon);
    }

    @PostMapping
    public Flux<WeatherResponseDto> getWeatherByRequest(
            @RequestBody WeatherRequestDto request) {
        return weatherService.getWeatherAsync(request);
    }

    @PostMapping("/batch")
    public Flux<WeatherResponseDto> getWeatherBatch(
            @RequestBody List<WeatherRequestDto.CityRequest> cities) {
        return weatherService.getWeatherForCitiesAsync(cities);
    }
}
