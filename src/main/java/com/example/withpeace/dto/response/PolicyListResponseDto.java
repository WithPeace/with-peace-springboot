package com.example.withpeace.dto.response;

import com.example.withpeace.domain.Policy;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;

public record PolicyListResponseDto(
        String id,
        String title,
        String introduce,
        EPolicyClassification classification,
        Set<EPolicyRegion> region,
        String ageInfo,
        String applicationPeriodStatus,
        boolean isFavorite) {

    public static PolicyListResponseDto from(Policy policy, boolean isFavorite) {
        return new PolicyListResponseDto(
                policy.getId(),
                policy.getTitle(),
                policy.getIntroduce(),
                policy.getClassification(),
                new HashSet<>(policy.getRegion()),
                policy.getAge(),
                policy.getApplicationPeriodStatus(),
                isFavorite
        );
    }
}
