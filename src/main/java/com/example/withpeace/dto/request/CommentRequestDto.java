package com.example.withpeace.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CommentRequestDto(
        @NotBlank @JsonProperty("content") @Schema(description = "내용") String content){
}
