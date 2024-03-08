package com.example.withpeace.dto.response;

import com.example.withpeace.dto.JwtTokenDto;
import com.example.withpeace.type.ERole;

public record LoginResponseDto(JwtTokenDto jwtTokenDto, ERole role) {
}
