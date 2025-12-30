package com.tetz.spring_proj.tetznight.dto.poll;

import com.tetz.spring_proj.tetznight.domain.poll.Poll;
import com.tetz.spring_proj.tetznight.domain.poll.PollOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PollResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean allowChangeVote;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Integer totalVotes;
    private Long userVotedOptionId;
    private List<PollOptionResponse> options;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PollOptionResponse {
        private Long id;
        private String optionText;
        private Integer displayOrder;
        private Integer voteCount;
        private Double percentage;
    }

    public static PollResponse from(Poll poll) {
        List<PollOptionResponse> optionResponses = poll.getOptions().stream()
                .map(option -> new PollOptionResponse(
                        option.getId(),
                        option.getOptionText(),
                        option.getDisplayOrder(),
                        0,  // voteCount
                        0.0  // percentage
                ))
                .collect(Collectors.toList());

        return new PollResponse(
                poll.getId(),
                poll.getTitle(),
                poll.getDescription(),
                poll.getStartDate(),
                poll.getEndDate(),
                poll.getIsActive(),
                poll.getAllowChangeVote(),
                poll.getCreatedBy(),
                poll.getCreatedAt(),
                0,  // totalVotes
                null,  // userVotedOptionId
                optionResponses
        );
    }

    public static PollResponse withVoteCounts(Poll poll, List<PollOption> options,
                                              int totalVotes, Long userVotedOptionId) {
        List<PollOptionResponse> optionResponses = options.stream()
                .map(option -> {
                    int voteCount = option.getVotes().size();
                    double percentage = totalVotes > 0 ?
                            Math.round(voteCount * 100.0 / totalVotes * 100.0) / 100.0 : 0.0;

                    return new PollOptionResponse(
                            option.getId(),
                            option.getOptionText(),
                            option.getDisplayOrder(),
                            voteCount,
                            percentage
                    );
                })
                .collect(Collectors.toList());

        return new PollResponse(
                poll.getId(),
                poll.getTitle(),
                poll.getDescription(),
                poll.getStartDate(),
                poll.getEndDate(),
                poll.getIsActive(),
                poll.getAllowChangeVote(),
                poll.getCreatedBy(),
                poll.getCreatedAt(),
                totalVotes,
                userVotedOptionId,
                optionResponses
        );
    }
}