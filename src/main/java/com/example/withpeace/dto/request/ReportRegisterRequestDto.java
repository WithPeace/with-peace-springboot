package com.example.withpeace.dto.request;

import com.example.withpeace.type.EReason;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReportRegisterRequestDto(
        @NotNull @JsonProperty("reason") @Schema(description = "신고이유") EReason reason){
}