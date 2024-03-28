package com.example.withpeace.dto.response;

import lombok.Builder;

@Builder
public record UserProfileResponseDto(Long userId, String email, String profileImageUrl, String nickname) {
}
