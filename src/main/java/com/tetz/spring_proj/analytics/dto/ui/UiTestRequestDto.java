package com.tetz.spring_proj.analytics.dto.ui;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "UI A/B 테스트 저장 요청 Type")
public class UiTestRequestDto {
    @Schema(description = "테스트 타입 (A or B)", example = "A")
    private String type;
    @Schema(description = "체류 시간 (단위는 ms, 60,000 = 1분)", example = "1000")
    private Long stayTime;
}
