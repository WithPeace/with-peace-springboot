package com.example.withpeace.dto.request;

import com.example.withpeace.type.EChoice;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record BalanceGameChoiceRequestDto(
        @NotNull @JsonProperty("choice") @Schema(description = "게임 선택") EChoice choice) {
}
