package com.tetz.spring_proj.tetznight.dto.poll;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PollCreateRequest {

    @NotBlank(message = "투표 제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
    private String title;

    @Size(max = 1000, message = "설명은 1000자 이내로 입력해주세요")
    private String description;

    @NotNull(message = "시작 일시는 필수입니다")
    private LocalDateTime startDate;

    @NotNull(message = "종료 일시는 필수입니다")
    private LocalDateTime endDate;

    private Boolean allowChangeVote = true;

    @NotNull(message = "투표 옵션은 최소 2개 이상 필요합니다")
    @Size(min = 2, message = "투표 옵션은 최소 2개 이상 필요합니다")
    private List<PollOptionRequest> options;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PollOptionRequest {
        @NotBlank(message = "옵션 텍스트는 필수입니다")
        @Size(max = 100, message = "옵션은 100자 이내로 입력해주세요")
        private String optionText;

        private Integer displayOrder;
    }
}