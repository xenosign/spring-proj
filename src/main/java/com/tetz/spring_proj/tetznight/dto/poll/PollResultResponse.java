package com.tetz.spring_proj.tetznight.dto.poll;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PollResultResponse {
    private Long pollId;
    private String title;
    private String description;
    private Integer totalVotes;
    private List<OptionResultResponse> results;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionResultResponse {
        private Long optionId;
        private String optionText;
        private Integer voteCount;
        private Double percentage;
        private Integer displayOrder;
    }
}