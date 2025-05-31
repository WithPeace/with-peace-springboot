package com.example.withpeace.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YouthPolicyApiResultResponseDto(
        @JsonProperty("pagging")
        YouthPolicyApiPagingResponseDto pagingInfo, // 페이징 정보

        @JsonProperty(value = "youthPolicyList")
        List<YouthPolicyApiResponseDto> policyList // 정책 리스트
) {}
