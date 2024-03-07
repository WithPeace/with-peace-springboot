package com.example.withpeace.service;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


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

    @Transactional
    public String updateProfileImage(Long userId, MultipartFile file) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        try {
            String fileUrl = "https://" + bucket + "/userProfile/" + userId;
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            amazonS3.putObject(bucket, "userProfile/" + userId, file.getInputStream(), metadata);
            user.updateProfileImage(fileUrl);

        } catch (Exception e) {
            throw new CommonException(ErrorCode.FILE_UPLOAD_ERROR);
        }
        // image url return
        return user.getProfileImage();
    }

    @Transactional
    public String deleteProfileImage(Long userId) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        amazonS3.deleteObject(bucket, "userProfile/" + userId);
        user.updateProfileImage("default.png");
        return user.getProfileImage();
    }


}
