package com.tetz.spring_proj.tetznight.dto.poll;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserVoteResponse {
    private Long pollId;
    private Long optionId;
    private String optionText;
    private Boolean hasVoted;

    public static UserVoteResponse notVoted(Long pollId) {
        return new UserVoteResponse(pollId, null, null, false);
    }

    public static UserVoteResponse voted(Long pollId, Long optionId, String optionText) {
        return new UserVoteResponse(pollId, optionId, optionText, true);
    }
}