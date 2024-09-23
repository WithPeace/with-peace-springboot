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
import jakarta.transaction.Transactional;
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

    public Boolean updateRegionAndClassification(Long userId, String region, String classification) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        updateAndGetRegionList(user, region);
        updateAndGetClassificationList(user, classification);

        return Boolean.TRUE;
    }

    @Transactional
    // 지역 정보를 처리 & User의 region 필드를 업데이트 & 필터링에 사용할 지역 리스트를 반환
    public List<EPolicyRegion> updateAndGetRegionList(User user, String region) {
        List<EPolicyRegion> regionList = Collections.emptyList();

        if (StringUtils.isNotBlank(region)) { // null, 빈 문자열, 공백만 있는 문자열을 모두 처리
            regionList = Arrays.stream(region.split(","))
                    .map(EPolicyRegion::fromCode)
                    .collect(Collectors.toList());
        }

        user.setRegions(regionList); // 사용자 지역 정보 업데이트
        userRepository.save(user); // User 테이블에 저장

        return user.getRegions();
    }

    @Transactional
    // 정책 분야 정보를 처리 & User의 classification 필드를 업데이트 & 필터링에 사용할 분야 리스트를 반환
    public List<EPolicyClassification> updateAndGetClassificationList(User user, String classification) {
        List<EPolicyClassification> classificationList = Collections.emptyList();

        if(StringUtils.isNotBlank(classification)) {
            classificationList = Arrays.stream(classification.split(","))
                    .map(EPolicyClassification::fromCode)
                    .collect(Collectors.toList());
        }

        user.setClassifications(classificationList); // 사용자 정책 분야 정보 업데이트
        userRepository.save(user); // User 테이블에 저장

        return user.getClassifications();
    }

}
