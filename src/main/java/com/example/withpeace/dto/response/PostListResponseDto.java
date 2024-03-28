package com.example.withpeace.dto.response;

import com.example.withpeace.domain.Post;
import com.example.withpeace.type.ETopic;
import com.example.withpeace.util.TimeFormatter;
import lombok.Builder;

@Builder
public record PostListResponseDto(
        Long postId,
        String title,
        String content,
        ETopic type,
        String createDate,
        String postImageUrl) {

    public static PostListResponseDto from(Post post, String postImageUrl) {
        return new PostListResponseDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getType(),
                TimeFormatter.timeFormat(post.getCreateDate()),
                postImageUrl
        );
    }
}
