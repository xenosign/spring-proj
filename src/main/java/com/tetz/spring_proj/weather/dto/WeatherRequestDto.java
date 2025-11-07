package com.tetz.spring_proj.weather.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Data
@Builder
public class WeatherRequestDto {
    // 단일 도시명 기반 검색
    private String cityName;
    private String countryCode;

    // 여러 도시 검색
    private List<CityRequest> cities;

    // 좌표 기반 검색
    private Double lat;
    private Double lon;

    // 공통 옵션
    private String units; // metric, imperial, standard
    private String lang;  // 언어 코드 (optional)

    public boolean isValid() {
        boolean hasCityName = cityName != null && !cityName.isEmpty();
        boolean hasCities = cities != null && !cities.isEmpty();
        boolean hasCoordinates = lat != null && lon != null;
        return hasCityName || hasCities || hasCoordinates;
    }

    @Data
    @Builder
    public static class CityRequest {
        private String cityName;
        private String countryCode;
    }
}
