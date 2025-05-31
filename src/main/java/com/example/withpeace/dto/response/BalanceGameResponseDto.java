package com.example.withpeace.dto.response;

import com.example.withpeace.type.EChoice;

import java.util.List;

public record BalanceGameResponseDto(
        Long gameId,
        String date, // "오늘의 밸런스게임" 또는 "n월 n일 밸런스게임"
        String title,
        String optionA,
        String optionB,
        EChoice userChoice, // "OPTION_A" 또는 "OPTION_B" 또는 null
        boolean isActive,
        Long optionACount,  // A 선택지 선택한 인원 수
        Long optionBCount,  // B 선택지 선택한 인원 수
        boolean hasPrevious,
        boolean hasNext,
        List<BalanceGameCommentListResponseDto> comments
) {
    public static BalanceGameResponseDto of(
            Long gameId,
            String date,
            String title,
            String optionA,
            String optionB,
            EChoice userChoice,
            boolean isActive,
            Long optionACount,
            Long optionBCount,
            boolean hasPrevious,
            boolean hasNext,
            List<BalanceGameCommentListResponseDto> comments
    ) {
        return new BalanceGameResponseDto(gameId, date, title, optionA, optionB, userChoice,
                isActive, optionACount, optionBCount, hasPrevious, hasNext, comments);
    }
}
