package com.example.withpeace.dto.response;

import com.example.withpeace.domain.Policy;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public record PolicyListResponseDto(
        String id,
        String title,
        String introduce,
        EPolicyClassification classification,
        List<EPolicyRegion> region,
        String ageInfo,
        String applicationPeriodStatus,
        boolean isFavorite) {

    public static PolicyListResponseDto from(Policy policy, boolean isFavorite) {
        return PolicyListResponseDto.builder()
                .id(policy.getId())
                .title(defaultIfNull(policy.getTitle()))
                .introduce(defaultIfNull(policy.getIntroduce()))
                .classification(policy.getClassification())
                .region(new ArrayList<>(policy.getRegion()))
                .ageInfo(defaultIfNull(policy.getAge()))
                .applicationPeriodStatus(policy.getApplicationPeriodStatus())
                .isFavorite(isFavorite)
                .build();
    }

    private static String defaultIfNull(String value) {
        return value != null ? value : "-";
    }
}
