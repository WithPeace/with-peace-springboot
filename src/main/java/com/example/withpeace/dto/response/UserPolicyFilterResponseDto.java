package com.example.withpeace.dto.response;

import com.example.withpeace.domain.User;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import lombok.Builder;

import java.util.List;

@Builder
public record UserPolicyFilterResponseDto(
        List<EPolicyRegion> region,
        List<EPolicyClassification> classification
) {
    public static UserPolicyFilterResponseDto from(User user) {
        return UserPolicyFilterResponseDto.builder()
                .region(user.getRegions())
                .classification(user.getClassifications())
                .build();
    }
}
