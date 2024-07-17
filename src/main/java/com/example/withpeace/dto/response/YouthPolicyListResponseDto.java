package com.example.withpeace.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record YouthPolicyListResponseDto(
        @JacksonXmlProperty(localName = "pageIndex")
        int pageIndex,

        @JacksonXmlProperty(localName = "totalCnt")
        int totalCount,

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "youthPolicy")
        List<YouthPolicyResponseDto> youthPolicyListResponseDto
) {}
