package com.example.withpeace.controller;

import com.example.withpeace.annotation.UserId;
import com.example.withpeace.constant.Constant;
import com.example.withpeace.dto.JwtTokenDto;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.dto.request.SocialRegisterRequestDto;
import com.example.withpeace.dto.response.LoginResponseDto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.service.AuthService;
import com.example.withpeace.type.EProvider;
import com.example.withpeace.util.HeaderUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;

    //구글 로그인
    @PostMapping("/google")
    public ResponseDto<LoginResponseDto> loginUsingGOOGLE(final HttpServletRequest request) throws IOException {
        final String accessToken = HeaderUtil.refineHeader(request, Constant.AUTHORIZATION_HEADER, Constant.BEARER_PREFIX).orElseThrow(() -> new CommonException(ErrorCode.SERVER_ERROR));
        final LoginResponseDto loginResponseDto = authService.loginForMobile(accessToken, EProvider.GOOGLE);
        return ResponseDto.ok(loginResponseDto);
    }

    //최초 회원가입
    @PostMapping("/register")
    public ResponseDto<JwtTokenDto> register(@UserId Long userId, @RequestBody @Valid SocialRegisterRequestDto socialRegisterRequestDto) {
        return ResponseDto.ok(authService.register(userId, socialRegisterRequestDto));
    }

    //Refresh Token 으로 Access Token 재발급
    @PostMapping("/refresh")
    public ResponseDto<?> refreshAccessToken(@RequestHeader("Authorization") String refreshToken) {
        return ResponseDto.ok(authService.refreshAccessToken(refreshToken));
    }

}
