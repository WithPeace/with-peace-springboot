package com.example.withpeace.service;

import com.example.withpeace.constant.Constant;
import com.example.withpeace.domain.User;
import com.example.withpeace.dto.JwtTokenDto;
import com.example.withpeace.dto.request.SocialRegisterDto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.UserRepository;
import com.example.withpeace.type.EProvider;
import com.example.withpeace.type.ERole;
import com.example.withpeace.util.JwtUtil;
import com.example.withpeace.util.OAuth2Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final OAuth2Util oAuth2Util;

    @Transactional
    public JwtTokenDto loginForMobile(final String accessToken, final EProvider loginProvider) {
        String socialId = null;

        switch (loginProvider) {

            case GOOGLE -> {
                socialId = oAuth2Util.getGoogleUserInformation(accessToken);
            }
            default -> {
                throw new CommonException(ErrorCode.INVALID_PROVIDER);
            }
        }

        if (socialId == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_USER);
        }

        Optional<User> userOpt = userRepository.findBySocialIdAndEProvider(socialId, loginProvider);

        User user = null;

        if (userOpt.isEmpty()) {
            user = userRepository.save(User.builder()
                    .socialId(socialId)
                    .eProvider(loginProvider)
                    .role(ERole.GUEST)
                    .build());

        } else {
            user = userOpt.get();
        }

        final JwtTokenDto jwtTokenDto = jwtUtil.generateTokens(user.getId(), user.getRole());
        user.setRefreshToken(jwtTokenDto.getRefreshToken());
        user.setLogin(true);

        return jwtTokenDto;
    }

    @Transactional
    public JwtTokenDto register(Long userId, SocialRegisterDto socialRegisterDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        user.register(socialRegisterDto.email(),socialRegisterDto.nickname(),socialRegisterDto.deviceToken());
        user.setRole(ERole.USER);
        user.setLogin(true);

        final JwtTokenDto jwtTokenDto = jwtUtil.generateTokens(user.getId(), user.getRole());
        user.setRefreshToken(jwtTokenDto.getRefreshToken());

        return jwtTokenDto;
    }

    public JwtTokenDto refreshAccessToken(String refreshToken) {
        UserRepository.UserSecurityForm user =
                userRepository.findByRefreshToken(refreshToken.substring(Constant.BEARER_PREFIX.length()))
                        .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole(), jwtUtil.getAccessTokenExpriration());
        return new JwtTokenDto(accessToken,refreshToken.substring(Constant.BEARER_PREFIX.length()));
    }

}
