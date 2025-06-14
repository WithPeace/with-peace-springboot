package com.example.withpeace.service;


import com.example.withpeace.domain.User;
import com.example.withpeace.dto.response.UserPolicyFilterResponseDto;
import com.example.withpeace.dto.response.UserProfileResponseDto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.UserRepository;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import io.micrometer.common.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

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
            String fileUrl;
            String blobName;
            if (user.getProfileImage() != null && !user.getProfileImage().equals("default.png")) {
                String oldBlobName = user.getProfileImage().substring(user.getProfileImage().indexOf(bucketName) + bucketName.length() + 1);
                storage.delete(BlobId.of(bucketName, oldBlobName));

                int lastIndex = oldBlobName.lastIndexOf("/") + 1;
                String folderPath = oldBlobName.substring(0, lastIndex);
                int newIndex = Integer.parseInt(oldBlobName.substring(lastIndex)) + 1;
                blobName = folderPath + newIndex;
            } else {
                blobName = "userProfile/" + userId + "/1";
            }

            fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + blobName;

            BlobId blobId = BlobId.of(bucketName, blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            storage.create(blobInfo, file.getBytes());

            user.updateProfileImage(fileUrl);
        } catch (Exception e) {
            throw new CommonException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    @Transactional
    public String deleteProfileImage(Long userId) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        String blobName = user.getProfileImage().substring(user.getProfileImage().indexOf(bucketName) + bucketName.length() + 1);
        storage.delete(BlobId.of(bucketName, blobName));
        user.updateProfileImage("default.png");
        return user.getProfileImage();
    }

    public Boolean checkNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Transactional
    public void updateRegionAndClassification(Long userId, String region, String classification) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        updateUserRegion(user, region); // 관심 지역 설정
        updateUserClassification(user, classification); // 관심 분야 설정
    }

    // 사용자 관심 지역(region) 정보 업데이트 (입력값이 비어있으면 관심 지역 초기화)
    public void updateUserRegion(User user, String region) {
        if (StringUtils.isNotBlank(region)) { // null, 빈 문자열, 공백 문제열 제외
            List<EPolicyRegion> regionList = Arrays.stream(region.split(","))
                    .map(EPolicyRegion::fromEnglishName)
                    .toList();
            user.setRegions(regionList); // 관심 지역 설정
        } else {
            user.setRegions(Collections.emptyList()); // 빈 문자열일 경우 초기화
        }
    }

    // 사용자 관심 분야(classification) 정보 업데이트 (입력값이 비어있으면 관심 분야 초기화)
    public void updateUserClassification(User user, String classification) {
        if(StringUtils.isNotBlank(classification)) {
            List<EPolicyClassification> classificationList = Arrays.stream(classification.split(","))
                    .map(EPolicyClassification::valueOf)
                    .toList();
            user.setClassifications(classificationList); // 관심 분야 설정
        } else {
            user.setClassifications(Collections.emptyList()); // 빈 문자열일 경우 초기화
        }
    }

    @Transactional(readOnly = true)
    public UserPolicyFilterResponseDto getRegionAndClassification(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        return UserPolicyFilterResponseDto.from(user);
    }

}
