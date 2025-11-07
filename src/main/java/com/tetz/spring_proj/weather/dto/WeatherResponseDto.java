package com.tetz.spring_proj.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class WeatherResponseDto {
    private Coord coord;
    private List<Weather> weather;
    private String base;
    private Main main;
    private Integer visibility;
    private Wind wind;
    private Rain rain;
    private Clouds clouds;
    private Long dt;
    private Sys sys;
    private Integer timezone;
    private Integer id;
    private String name;
    private Integer cod;
}

@Data
class Coord {
    private Double lon;
    private Double lat;
}

@Data
class Weather {
    private Integer id;
    private String main;
    private String description;
    private String icon;
}

@Data
class Main {
    private Double temp;

    @JsonProperty("feels_like")
    private Double feelsLike;

    @JsonProperty("temp_min")
    private Double tempMin;

    @JsonProperty("temp_max")
    private Double tempMax;

    private Integer pressure;
    private Integer humidity;

    @JsonProperty("sea_level")
    private Integer seaLevel;

    @JsonProperty("grnd_level")
    private Integer grndLevel;
}

@Data
class Wind {
    private Double speed;
    private Integer deg;
    private Double gust;
}

@Data
class Rain {
    @JsonProperty("1h")
    private Double oneHour;
}

@Data
class Clouds {
    private Integer all;
}

@Data
class Sys {
    private Integer type;
    private Integer id;
    private String country;
    private Long sunrise;
    private Long sunset;
}
