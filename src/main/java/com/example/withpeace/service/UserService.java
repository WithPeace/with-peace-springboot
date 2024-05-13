package com.example.withpeace.service;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.withpeace.domain.User;
import com.example.withpeace.dto.response.UserProfileResponseDto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.s3.static}")
    private String endpoint;

    public Boolean withdrawalUser(Long userId) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        userRepository.delete(user);
        user.setDeleteDate();
        return Boolean.TRUE;
    }

    @Transactional
    public Boolean recoveryUser(String email) {
        User user =
                userRepository.findByEmail(email).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        user.recoveryUser();
        return Boolean.TRUE;
    }

    public UserProfileResponseDto getUserProfile(Long userId) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        UserProfileResponseDto userProfileResponseDto =
                UserProfileResponseDto.builder()
                        .userId(user.getId())
                        .profileImageUrl(user.getProfileImage())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .build();

        return userProfileResponseDto;
    }

    @Transactional
    public UserProfileResponseDto updateProfile(Long userId, String nickname, MultipartFile file) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        user.updateNickname(nickname);
        if (file != null) {
            uploadProfileImage(userId, file, user);
        }
        UserProfileResponseDto userProfileResponseDto =
                UserProfileResponseDto.builder()
                        .userId(user.getId())
                        .profileImageUrl(user.getProfileImage())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .build();

        return userProfileResponseDto;
    }

    @Transactional
    public String updateProfile(User user, String nickname, MultipartFile file) {
        user.updateNickname(nickname);
        if (file != null) {
            uploadProfileImage(user.getId(), file, user);
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
        uploadProfileImage(userId, file, user);
        // image url return
        return user.getProfileImage();
    }

    @Transactional
    public void uploadProfileImage(Long userId, MultipartFile file, User user) {
        try {
            String fileUrl = null;
            if (user.getProfileImage() != null && !user.getProfileImage().equals("default.png")) {
                int index = endpoint.length() + 1;
                int lastIndex = user.getProfileImage().lastIndexOf("/") + 1;
                fileUrl = endpoint + "/" + user.getProfileImage().substring(index, lastIndex) + (Integer.parseInt(user.getProfileImage().substring(lastIndex)) + 1);
                amazonS3.deleteObject(bucket, user.getProfileImage().substring(index));
            } else {
                fileUrl = endpoint + "/userProfile/" + userId + "/1";
            }
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            amazonS3.putObject(bucket, fileUrl.substring(endpoint.length() + 1), file.getInputStream(), metadata);
            user.updateProfileImage(fileUrl);

        } catch (Exception e) {
            throw new CommonException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    @Transactional
    public String deleteProfileImage(Long userId) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        int index = endpoint.length() + 1;
        amazonS3.deleteObject(bucket, user.getProfileImage().substring(index));
        user.updateProfileImage("default.png");
        return user.getProfileImage();
    }

    public Boolean checkNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


}
