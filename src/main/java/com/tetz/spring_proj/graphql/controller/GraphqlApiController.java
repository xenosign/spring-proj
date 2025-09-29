package com.tetz.spring_proj.graphql.controller;

import com.tetz.spring_proj.exchange.dto.ExchangeRateResponseDTO;
import com.tetz.spring_proj.exchange.service.ExchangeRateService;
import com.tetz.spring_proj.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class GraphqlApiController {
    private final ExchangeRateService exchangeRateService;
    private final TodoService todoService;

    @QueryMapping
    public ExchangeRateResponseDTO specificExchangeRates(@Argument List<String> currencyCodes) {
        return exchangeRateService.getSpecificExchangeRatesAsync(currencyCodes);
    }
}
