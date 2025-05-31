package com.example.withpeace.dto.response;

import com.example.withpeace.domain.Post;
import com.example.withpeace.type.ETopic;

public record RecentPostResponseDto(
        ETopic type,
        Long postId,
        String title
) {
    public static RecentPostResponseDto from(Post post) {
        return new RecentPostResponseDto(
                post.getType(),
                post.getId(),
                post.getTitle()
        );
    }
}
