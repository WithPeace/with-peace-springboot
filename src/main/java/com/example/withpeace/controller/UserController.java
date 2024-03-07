package com.example.withpeace.controller;


import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.service.UserService;
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

    //회원탈퇴
    @DeleteMapping("")
    public ResponseDto<?> withdrawalUser(@UserId Long userId) {
        return ResponseDto.ok(userService.withdrawalUser(userId));
    }

    @PutMapping("profile/image")
    public ResponseDto<?> updateProfileImage(@UserId Long userId, @RequestParam("file") MultipartFile file) {
        return ResponseDto.ok(userService.updateProfileImage(userId, file));
    }

    @DeleteMapping("profile/image")
    public ResponseDto<?> deleteProfileImage(@UserId Long userId) {
        return ResponseDto.ok(userService.deleteProfileImage(userId));
    }


}
