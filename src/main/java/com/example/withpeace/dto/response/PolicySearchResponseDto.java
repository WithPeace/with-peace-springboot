package com.example.withpeace.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PolicySearchResponseDto(
        List<PolicyListResponseDto> policies,
        long totalCount
) {
    public static PolicySearchResponseDto of(List<PolicyListResponseDto> policies, long totalCount) {
        return new PolicySearchResponseDto(policies, totalCount);
    }
}
