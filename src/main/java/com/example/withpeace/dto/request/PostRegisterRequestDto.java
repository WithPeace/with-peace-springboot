package com.example.withpeace.dto.request;

import com.example.withpeace.type.ETopic;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostRegisterRequestDto(
        @NotBlank @JsonProperty("title") @Schema(description = "제목") String title,
        @NotBlank @JsonProperty("content") @Schema(description = "내용") String content,
        @NotNull @JsonProperty("type") @Schema(description = "주제") ETopic type){
}
