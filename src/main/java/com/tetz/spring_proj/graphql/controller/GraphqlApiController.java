package com.tetz.spring_proj.graphql.controller;

import com.tetz.spring_proj.exchange.dto.ExchangeRateResponseDto;
import com.tetz.spring_proj.exchange.service.ExchangeRateService;
import com.tetz.spring_proj.todo.dto.TodoResponseDto;
import com.tetz.spring_proj.todo.service.TodoService;
import com.tetz.spring_proj.weather.dto.WeatherResponseDto;
import com.tetz.spring_proj.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GraphqlApiController {
    private final ExchangeRateService exchangeRateService;
    private final TodoService todoService;
    private final WeatherService weatherService;

    @QueryMapping
    public ExchangeRateResponseDto allExchangeRates() {
        return exchangeRateService.getAllExchangeRatesAsync();
    }

    @QueryMapping
    public ExchangeRateResponseDto specificExchangeRates(@Argument List<String> currencyCodes) {
        return exchangeRateService.getSpecificExchangeRatesAsync(currencyCodes);
    }

    @QueryMapping
    public List<TodoResponseDto> getAllTodos(@AuthenticationPrincipal String userId) {
        return todoService.getAllTodosByUserId(userId);
    }

    @QueryMapping
    public Mono<WeatherResponseDto> getWeather(
            @Argument String city,
            @Argument String country) {
        return weatherService.getWeatherAsync(city, country);
    }
}
