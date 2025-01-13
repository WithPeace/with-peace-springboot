package com.example.withpeace.dto.response;

import com.example.withpeace.type.EChoice;

public record BalanceGameCommentListResponseDto(
        Long commentId,
        Long userId,
        String nickname,
        String profileImageUrl,
        String content,
        EChoice userChoice, // "OPTION_A" 또는 "OPTION_B" 또는 null
        String createDate
) {
}
