package com.example.withpeace.controller;

import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.service.AppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/app")
public class AppController {

    private final AppService appService;

    // 안드로이드 강제 업데이트 검사
    @GetMapping("/check/android")
    public ResponseDto<?> checkAndroidForceUpdate(@RequestParam int currentVersion) {
        boolean forceUpdate = appService.checkAndroidForceUpdate(currentVersion);
        return ResponseDto.ok(forceUpdate);
    }

    // 안드로이드 강제 업데이트 버전 설정
    @PostMapping("/setVersion/android")
    public ResponseDto<?> setAndroidForceUpdateVersion(@UserId Long userId,
                                                       @Valid @RequestBody int updateVersion) {
        int updatedVersion = appService.setAndroidForceUpdateVersion(userId, updateVersion);
        return ResponseDto.ok(updatedVersion);
    }
}
