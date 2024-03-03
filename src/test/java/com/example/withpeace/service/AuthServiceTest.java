package com.example.withpeace.service;

import com.example.withpeace.domain.User;
import com.example.withpeace.dto.JwtTokenDto;
import com.example.withpeace.dto.request.SocialRegisterDto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.repository.UserRepository;
import com.example.withpeace.type.EProvider;
import com.example.withpeace.type.ERole;
import com.example.withpeace.util.JwtUtil;
import com.example.withpeace.util.OAuth2Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OAuth2Util oAuth2Util;

    @Test
    @DisplayName("사용자 로그인 성공: 유효한 액세스 토큰과 사용자를 제공했을 때 로그인이 성공적으로 이루어지는지를 검증")
    void loginForMobileSuccess() throws IOException {
        // Given
        String accessToken = "valid_accessToken";
        EProvider loginProvider = EProvider.GOOGLE;
        String socialId = "valid_socialId";

        given(oAuth2Util.getGoogleUserInformation(accessToken)).willReturn(socialId); // socialId를 반환하도록 목 설정
        given(userRepository.findBySocialIdAndEProvider(socialId, loginProvider)).willReturn(Optional.empty()); // 사용자를 찾을 때 빈 Optional을 반환하도록 목 설정

        User user = User.builder()
                .socialId(socialId)
                .eProvider(loginProvider)
                .role(ERole.GUEST)
                .build();
        given(userRepository.save(any(User.class))).willReturn(user); // User 객체를 반환하도록 목 설정
        // JWT 토큰을 생성하도록 목 설정
        JwtTokenDto jwtTokenDto = new JwtTokenDto("test_accessToken", "test_refreshToken");
        given(jwtUtil.generateTokens(user.getId(), user.getRole())).willReturn(jwtTokenDto);

        // When
        JwtTokenDto result = authService.loginForMobile(accessToken, loginProvider);

        // Then
        assertNotNull(result); // 반환된 결과가 null이 아닌지 확인
        assertEquals(jwtTokenDto, result); // 반환된 JWT 토큰이 예상한 JWT 토큰과 일치하는지 검증
        verify(oAuth2Util, times(1)).getGoogleUserInformation(accessToken);
        verify(userRepository, times(1)).findBySocialIdAndEProvider(socialId, loginProvider);
        verify(jwtUtil, times(1)).generateTokens(user.getId(), user.getRole());
    }

}