package com.example.withpeace.controller;

import com.auth0.jwk.JwkException;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Google 로그인", description = "Google 클라이언트 ID를 이용하여 로그인합니다.", tags = ("Auth"),
            security = { @SecurityRequirement(name = "Social Auth") })
    @PostMapping("/google")
    public ResponseDto<LoginResponseDto> loginUsingGOOGLE(final HttpServletRequest request) throws IOException, JwkException {
        final String accessToken = HeaderUtil.refineHeader(request, Constant.AUTHORIZATION_HEADER, Constant.BEARER_PREFIX).orElseThrow(() -> new CommonException(ErrorCode.SERVER_ERROR));
        final LoginResponseDto loginResponseDto = authService.loginForMobile(accessToken, EProvider.GOOGLE);
        return ResponseDto.ok(loginResponseDto);
    }

    @Operation(summary = "Apple 로그인", description = "Apple 클라이언트 ID를 이용하여 로그인합니다.", tags = ("Auth"),
            security = { @SecurityRequirement(name = "Social Auth") })
    @PostMapping("/apple")
    public ResponseDto<LoginResponseDto> loginUsingApple(final HttpServletRequest request) throws IOException, JwkException {
        final String accessToken = HeaderUtil.refineHeader(request, Constant.AUTHORIZATION_HEADER, Constant.BEARER_PREFIX).orElseThrow(() -> new CommonException(ErrorCode.SERVER_ERROR));
        final LoginResponseDto loginResponseDto = authService.loginForMobile(accessToken, EProvider.APPLE);
        return ResponseDto.ok(loginResponseDto);
    }

    @Operation(summary = "회원가입",
            description = "소셜 로그인 후 최초 회원가입을 수행합니다. 닉네임과 프로필 이미지(선택)를 설정할 수 있습니다.", tags = {"Auth"})
    @PostMapping("/register")
    public ResponseDto<JwtTokenDto> register(@UserId Long userId, @ModelAttribute @Valid SocialRegisterRequestDto socialRegisterRequestDto,
                                            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        return ResponseDto.ok(authService.register(userId, socialRegisterRequestDto, imageFile));
    }

    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 이용하여 새로운 액세스 토큰을 발급받습니다.", tags = {"Auth"})
    @PostMapping("/refresh")
    public ResponseDto<?> refreshAccessToken(@RequestHeader("Authorization") String refreshToken) {
        return ResponseDto.ok(authService.refreshAccessToken(refreshToken));
    }

}
