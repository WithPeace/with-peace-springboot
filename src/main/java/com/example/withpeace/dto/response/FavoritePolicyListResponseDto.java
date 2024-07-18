package com.example.withpeace.dto.response;

import com.example.withpeace.domain.FavoritePolicy;
import com.example.withpeace.domain.YouthPolicy;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import lombok.Builder;

@Builder
public record FavoritePolicyListResponseDto(
        String id,
        String title,
        String introduce,
        EPolicyClassification classification,
        EPolicyRegion region,
        String ageInfo,
        boolean isActive) {

    public static FavoritePolicyListResponseDto from(YouthPolicy policy, boolean isActive) {
        return new FavoritePolicyListResponseDto(
                policy.getId(),
                policy.getTitle(),
                policy.getIntroduce(),
                policy.getClassification(),
                policy.getRegion(),
                policy.getAgeInfo(),
                isActive
        );
    }

    public static FavoritePolicyListResponseDto from(FavoritePolicy favoritePolicy) {
        return new FavoritePolicyListResponseDto(
                favoritePolicy.getPolicyId(),
                favoritePolicy.getTitle(),
                null, // introduce
                null, // classification
                null, // region
                null, // ageInfo
                favoritePolicy.isActive()
        );
    }
}
