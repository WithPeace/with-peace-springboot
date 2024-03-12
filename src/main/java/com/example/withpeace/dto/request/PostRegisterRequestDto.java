package com.example.withpeace.dto.request;

import com.example.withpeace.type.ETopic;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.util.List;

public record PostRegisterRequestDto(
        @NotBlank @JsonProperty("title") String title,
        @NotBlank @JsonProperty("content") String content,
        @NotNull @JsonProperty("type") ETopic type){
}
