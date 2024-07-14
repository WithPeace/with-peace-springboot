package com.example.withpeace.dto.response;

import lombok.Builder;

@Builder
public record CommentListResponseDto(
        Long commentId,
        Long userId,
        String nickname,
        String profileImageUrl,
        String content,
        String createDate) {
}
