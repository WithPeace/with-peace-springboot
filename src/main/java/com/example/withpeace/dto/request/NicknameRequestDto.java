package com.example.withpeace.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NicknameRequestDto(
        @Size(min = 4, max = 10, message = "닉네임은 4자 이상 10자 이하이어야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "닉네임에 특수문자는 사용할 수 없습니다.")
        @JsonProperty("nickname") String nickname){
}
