package com.tetz.spring_proj.tetznight.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeResponse {
    private Long commentId;
    private Integer likeCount;
    private Boolean isLiked;  // 현재 사용자가 좋아요를 눌렀는지 여부

    public static CommentLikeResponse of(Long commentId, Integer likeCount, Boolean isLiked) {
        return new CommentLikeResponse(commentId, likeCount, isLiked);
    }
}
