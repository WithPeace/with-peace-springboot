package com.example.withpeace.dto.response;

import com.example.withpeace.type.ETopic;
import lombok.Builder;

import java.util.List;

@Builder
public record PostDetailResponseDto(
        Long postId,
        Long userId,
        String nickname,
        String profileImageUrl,
        String title,
        String content,
        ETopic type,
        String createDate,
        List<String> postImageUrls) {
}
