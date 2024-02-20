package com.example.withpeace.service;


import com.example.withpeace.constant.Constant;
import com.example.withpeace.domain.User;
import com.example.withpeace.dto.JwtTokenDto;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.dto.request.SocialRegisterDto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.UserRepository;
import com.example.withpeace.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    @Transactional
    public JwtTokenDto socialRegisterUser(Long userId, SocialRegisterDto socialRegisterDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CommonException(ErrorCode.NOT_FOUND_USER));
//        user.register(socialRegisterDto.nickname(), socialRegisterDto.phoneNumber());

        final JwtTokenDto jwtTokenDto = jwtUtil.generateTokens(user.getId(), user.getRole());
        user.setRefreshToken(jwtTokenDto.getRefreshToken());

        return jwtTokenDto;
    }

    public ResponseDto<Boolean> withdrawalUser(Long userId) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        userRepository.delete(user);

        return ResponseDto.ok(Boolean.TRUE);
    }



}
