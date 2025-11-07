package com.tetz.spring_proj.weather.service;

import com.tetz.spring_proj.weather.dto.WeatherRequestDto;
import com.tetz.spring_proj.weather.dto.WeatherResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

// Todo
// 환율도 WebClient 방식으로 변경 및 Security 필터에 Reactive 적용 필요

@Slf4j
@Service
public class WeatherService {
    @Value("${weather.open-weather.api-key}")
    private String apiKey;

    private final WebClient webClient;

    public WeatherService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.openweathermap.org/data/2.5")
                .build();
    }

    // 단일 도시 조회
    public Mono<WeatherResponseDto> getWeatherAsync(String cityName, String countryCode) {
        String qParam = cityName + (countryCode != null ? "," + countryCode : "");

        return webClient.get()
                .uri(uriBuilder -> {
                    var uri = uriBuilder
                            .path("/weather")
                            .queryParam("q", qParam)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build();


                    log.info("Request URI: {}", uri);

                    return uri;
                })
                .retrieve()
                .bodyToMono(WeatherResponseDto.class)
                .doOnSuccess(response -> log.info("Response received for city: {}",
                        response != null ? response.getName() : "null"))
                .doOnError(error -> log.error("Error fetching weather for {}: {}",
                        cityName, error.getMessage()));
    }


    // 여러 도시 조회 (도시명 + 국가코드)
    public Flux<WeatherResponseDto> getWeatherForCitiesAsync(List<WeatherRequestDto.CityRequest> cities) {
        return Flux.fromIterable(cities)
                .flatMap(city -> getWeatherAsync(city.getCityName(), city.getCountryCode()));
    }

    // 좌표로 단일 조회
    public Mono<WeatherResponseDto> getWeatherByCoordinatesAsync(Double lat, Double lon) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .build())
                .retrieve()
                .bodyToMono(WeatherResponseDto.class);
    }

    // WeatherRequestDto로 조회 (단일 도시, 여러 도시, 좌표 모두 지원)
    public Flux<WeatherResponseDto> getWeatherAsync(WeatherRequestDto request) {
        if (!request.isValid()) {
            return Flux.error(new IllegalArgumentException("도시명 또는 좌표 정보가 필요합니다."));
        }

        // 여러 도시 조회
        if (request.getCities() != null && !request.getCities().isEmpty()) {
            return getWeatherForCitiesAsync(request.getCities());
        }

        // 단일 조회 (좌표 또는 도시명)
        return getSingleWeather(request).flux();
    }

    // 단일 날씨 조회 (좌표 또는 도시명)
    private Mono<WeatherResponseDto> getSingleWeather(WeatherRequestDto request) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/weather").queryParam("appid", apiKey);

                    if (request.getLat() != null && request.getLon() != null) {
                        uriBuilder.queryParam("lat", request.getLat())
                                .queryParam("lon", request.getLon());
                    } else if (request.getCityName() != null) {
                        String qParam = request.getCityName();
                        if (request.getCountryCode() != null) {
                            qParam += "," + request.getCountryCode();
                        }
                        uriBuilder.queryParam("q", qParam);
                    }

                    if (request.getUnits() != null) {
                        uriBuilder.queryParam("units", request.getUnits());
                    }
                    if (request.getLang() != null) {
                        uriBuilder.queryParam("lang", request.getLang());
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(WeatherResponseDto.class);
    }
}