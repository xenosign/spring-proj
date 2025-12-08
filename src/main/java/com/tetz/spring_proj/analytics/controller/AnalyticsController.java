package com.tetz.spring_proj.analytics.controller;

import com.tetz.spring_proj.analytics.dto.ui.UiTestRequestDto;
import com.tetz.spring_proj.analytics.service.ui.UiTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "A/B TEST API", description = "A/B 테스트를 위한 API 모음")
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {
    private final UiTestService uiTestService;

    @Operation(summary = "UI A/B TEST 결과 저장", description = "UI A/B 테스트 결과를 저장")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "403", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "저장 상황에서 서버 에러 발생")
    })
    @PostMapping("/ui-test")
    public ResponseEntity<Void> saveUiTestLog(
            @AuthenticationPrincipal String userId,
            @RequestBody UiTestRequestDto uiTestRequestDto
    ) {
        uiTestService.saveUiLog(Long.parseLong(userId), uiTestRequestDto);

        return ResponseEntity.ok().build();
    }
}
