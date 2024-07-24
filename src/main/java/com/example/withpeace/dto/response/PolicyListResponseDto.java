package com.example.withpeace.dto.response;

import com.example.withpeace.domain.YouthPolicy;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import lombok.Builder;

@Builder
public record PolicyListResponseDto(
        String id,
        String title,
        String introduce,
        EPolicyClassification classification,
        EPolicyRegion region,
        String ageInfo,
        boolean isFavorite) {

    public static PolicyListResponseDto from(YouthPolicy policy, boolean isFavorite) {
        return new PolicyListResponseDto(
                policy.getId(),
                policy.getTitle(),
                policy.getIntroduce(),
                policy.getClassification(),
                policy.getRegion(),
                policy.getAgeInfo(),
                isFavorite
        );
    }
}
