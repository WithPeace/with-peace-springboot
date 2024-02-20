package com.example.withpeace.dto;


import com.example.withpeace.exception.ErrorCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ExceptionDto{
    @Min(40000)
    private Integer code;
    @NotBlank
    private String message;

    public ExceptionDto(ErrorCode errorCode, String message) {
        this.code = errorCode.getCode();
        this.message = message;
    }
}
