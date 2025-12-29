package com.tetz.spring_proj.tetznight.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {
    @NotNull(message = "투표 ID는 필수입니다")
    private Long pollId;

    @NotNull(message = "옵션 ID는 필수입니다")
    private Long optionId;
}