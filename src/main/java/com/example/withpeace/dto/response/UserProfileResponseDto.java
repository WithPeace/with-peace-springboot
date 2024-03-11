package com.example.withpeace.dto.response;

import lombok.Builder;

@Builder
public record UserProfileResponseDto(String email, String profileImageUrl, String nickname) {
}
