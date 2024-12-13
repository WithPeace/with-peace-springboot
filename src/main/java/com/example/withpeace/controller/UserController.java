package com.example.withpeace.controller;


import com.amazonaws.Response;
import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.dto.request.NicknameRequestDto;
import com.example.withpeace.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @Operation(summary = "프로필 조회", description = "사용자의 프로필 정보를 조회합니다.", tags = {"User"})
    @GetMapping("profile")
    public ResponseDto<?> getUserProfile(@UserId Long userId) {
        return ResponseDto.ok(userService.getUserProfile(userId));
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.", tags = {"User"})
    @GetMapping("profile/nickname/check")
    public ResponseDto<?> checkNickname(@RequestParam String nickname) {
        return ResponseDto.ok(userService.checkNickname(nickname));
    }

    @Operation(summary = "회원 탈퇴", description = "회원 계정을 비활성화합니다.", tags = {"User"})
    @DeleteMapping("")
    public ResponseDto<?> withdrawalUser(@UserId Long userId) {
        return ResponseDto.ok(userService.withdrawalUser(userId));
    }

    @Operation(summary = "계정 복구", description = "탈퇴한 계정을 복구합니다.", tags = {"User"})
    @PatchMapping("recovery")
    public ResponseDto<?> recoveryUser(@RequestBody @Email String email) {
        return ResponseDto.ok(userService.recoveryUser(email));
    }

    @Operation(summary = "프로필 수정", description = "닉네임과 프로필 이미지를 함께 수정합니다.", tags = {"User"})
    @PutMapping(value = "profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<?> updateProfile(@UserId Long userId, @Valid @ModelAttribute NicknameRequestDto nickname,
                                        @Parameter(description = "Image file", required = true, content = @Content(mediaType = "multipart/form-data")) @RequestPart("imageFile") MultipartFile file) {
        return ResponseDto.ok(userService.updateProfile(userId, nickname.nickname(), file));
    }

    @Operation(summary = "닉네임 수정", description = "닉네임만 수정합니다.", tags = {"User"})
    @PatchMapping("profile/nickname")
    public ResponseDto<?> updateNickname(@UserId Long userId, @Valid @RequestBody NicknameRequestDto nickname) {
        return ResponseDto.ok(userService.updateNickname(userId, nickname.nickname()));
    }

    @Operation(summary = "프로필 이미지 수정", description = "프로필 이미지만 수정합니다.", tags = {"User"})
    @PatchMapping(value = "profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<?> updateProfileImage(@UserId Long userId, @RequestPart("imageFile") MultipartFile file) {
        return ResponseDto.ok(userService.updateProfileImage(userId, file));
    }

    @Operation(summary = "프로필 이미지 삭제", description = "프로필 이미지를 기본 이미지로 변경합니다.", tags = {"User"})
    @DeleteMapping("profile/image")
    public ResponseDto<?> deleteProfileImage(@UserId Long userId) {
        return ResponseDto.ok(userService.deleteProfileImage(userId));
    }

    @Operation(summary = "정책 필터 설정", description = "관심 지역과 분야를 설정합니다.", tags = {"User"})
    @PatchMapping("profile/policy-filter")
    public ResponseDto<?> updateRegionAndClassification(@UserId Long userId,
                                                    @RequestParam(defaultValue = "") String region,
                                                    @RequestParam(defaultValue = "") String classification) {
        return ResponseDto.ok(userService.updateRegionAndClassification(userId, region, classification));
    }

    @Operation(summary = "정책 필터 조회", description = "설정된 관심 지역과 분야를 조회합니다.", tags = {"User"})
    @GetMapping("profile/policy-filter")
    public ResponseDto<?> getRegionAndClassification(@UserId Long userId) {
        return ResponseDto.ok(userService.getRegionAndClassification(userId));
    }
}
