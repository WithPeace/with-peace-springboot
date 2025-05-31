package com.example.withpeace.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YouthPolicyApiPagingResponseDto(
        @JsonProperty("totCount")
        int totalCount // 정책 개수
) {}