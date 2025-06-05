package com.example.withpeace.dto.response;

import com.example.withpeace.type.EPolicyClassification;
import lombok.Builder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis 캐시 전용 DTO
 * - isFavorite 제외
 * - 직렬화를 위한 Serializable 구현
 */
@Builder
public record PolicyCacheListResponseDto(
        String id,
        String title,
        String introduce,
        EPolicyClassification classification,
        List<String> region,
        String ageInfo,
        String applicationPeriodStatus) implements Serializable {

    // PolicyListResponseDto -> 캐시용 DTO로 변환
    public static PolicyCacheListResponseDto from(PolicyListResponseDto dto) {
        return PolicyCacheListResponseDto.builder()
                .id(dto.id())
                .title(dto.title())
                .introduce(dto.introduce())
                .classification(dto.classification())
                // Enum은 name()으로 String으로 변환해 Redis 저장 시 직렬화 오류 방지
                .region(dto.region().stream().map(Enum::name).toList())
                .ageInfo(dto.ageInfo())
                .applicationPeriodStatus(dto.applicationPeriodStatus())
                .build();
    }
}
