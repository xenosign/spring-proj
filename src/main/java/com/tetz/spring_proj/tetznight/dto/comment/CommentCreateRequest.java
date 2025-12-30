package com.tetz.spring_proj.tetznight.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {

    @NotNull(message = "투표 ID는 필수입니다")
    private Long pollId;

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(max = 50, message = "닉네임은 50자 이내로 입력해주세요")
    private String nickname;

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 500, message = "댓글은 500자 이내로 입력해주세요")
    private String comment;
}