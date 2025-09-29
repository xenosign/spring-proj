package com.tetz.spring_proj.graphql.controller;

import com.tetz.spring_proj.exchange.dto.ExchangeRateResponseDto;
import com.tetz.spring_proj.exchange.service.ExchangeRateService;
import com.tetz.spring_proj.todo.dto.TodoResponseDto;
import com.tetz.spring_proj.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GraphqlApiController {
    private final ExchangeRateService exchangeRateService;
    private final TodoService todoService;

    @QueryMapping
    public ExchangeRateResponseDto specificExchangeRates(@Argument List<String> currencyCodes) {
        return exchangeRateService.getSpecificExchangeRatesAsync(currencyCodes);
    }

    @QueryMapping
    public List<TodoResponseDto> getAllTodos(@AuthenticationPrincipal String userId) {
        return todoService.getAllTodosByUserId(userId);
    }
}
