package com.example.withpeace.dto.request;


import com.example.withpeace.type.EProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import javax.annotation.Nullable;

public record SocialRegisterDto (
        @NotBlank @JsonProperty("email") String email,
        @NotBlank @JsonProperty("nickname") String nickname,
        @Nullable @JsonProperty("deviceToken") String deviceToken){
}
