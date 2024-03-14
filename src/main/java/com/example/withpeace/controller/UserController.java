package com.example.withpeace.controller;


import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.dto.request.NicknameRequestDto;
import com.example.withpeace.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("profile")
    public ResponseDto<?> getUserProfile(@UserId Long userId) {
        return ResponseDto.ok(userService.getUserProfile(userId));
    }

    //회원탈퇴
    @DeleteMapping("")
    public ResponseDto<?> withdrawalUser(@UserId Long userId) {
        return ResponseDto.ok(userService.withdrawalUser(userId));
    }

    @PutMapping("profile")
    public ResponseDto<?> updateProfile(@UserId Long userId, @Valid @ModelAttribute NicknameRequestDto nickname,
                                        @RequestPart("imageFile") MultipartFile file) {
        return ResponseDto.ok(userService.updateProfile(userId,nickname.nickname(),file));
    }

    @PatchMapping("profile/nickname")
    public ResponseDto<?> updateNickname(@UserId Long userId, @Valid @RequestBody NicknameRequestDto nickname) {
        return ResponseDto.ok(userService.updateNickname(userId, nickname.nickname()));
    }

    @PatchMapping("profile/image")
    public ResponseDto<?> updateProfileImage(@UserId Long userId, @RequestPart("imageFile") MultipartFile file) {
        return ResponseDto.ok(userService.updateProfileImage(userId, file));
    }

    @DeleteMapping("profile/image")
    public ResponseDto<?> deleteProfileImage(@UserId Long userId) {
        return ResponseDto.ok(userService.deleteProfileImage(userId));
    }


}
