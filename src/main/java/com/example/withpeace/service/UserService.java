package com.example.withpeace.service;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.withpeace.domain.User;
import com.example.withpeace.dto.response.UserProfileResponseDto;
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
    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    public Boolean withdrawalUser(Long userId) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        userRepository.delete(user);

        return Boolean.TRUE;
    }

    public UserProfileResponseDto getUserProfile(Long userId) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        UserProfileResponseDto userProfileResponseDto =
                UserProfileResponseDto.builder()
                        .profileImageUrl(user.getProfileImage())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .build();

        return userProfileResponseDto;
    }

    @Transactional
    public String updateProfile(Long userId, String nickname, MultipartFile file) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        user.updateNickname(nickname);
        if (file != null) {
            String fileUrl = endpoint + "/" + bucket + "/userProfile/" + userId;
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            try {
                amazonS3.putObject(bucket, "userProfile/" + userId, file.getInputStream(), metadata);
                user.updateProfileImage(fileUrl);
            } catch (Exception e) {
                throw new CommonException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }
        return user.getProfileImage();
    }

    @Transactional
    public String updateNickname(Long userId, String nickname) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        user.updateNickname(nickname);
        return user.getNickname();
    }

    @Transactional
    public String updateProfileImage(Long userId, MultipartFile file) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        try {
            String fileUrl = endpoint + "/" + bucket + "/userProfile/" + userId;
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
