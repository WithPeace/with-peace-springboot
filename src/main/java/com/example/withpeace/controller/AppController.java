package com.example.withpeace.controller;

import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/app")
public class AppController {

    private final AppService appService;

    @Operation(summary = "안드로이드 앱 업데이트 확인", description = "안드로이드 앱의 강제 업데이트 필요 여부를 확인합니다.", tags = {"App"})
    @GetMapping("/check/android")
    public ResponseDto<?> checkAndroidForceUpdate(@RequestParam int currentVersion) {
        boolean forceUpdate = appService.checkAndroidForceUpdate(currentVersion);
        return ResponseDto.ok(forceUpdate);
    }

    @Operation(summary = "안드로이드 강제 업데이트 버전 설정 (관리자)",
            description = "안드로이드 앱의 강제 업데이트 버전을 설정합니다. 관리자만 요청할 수 있습니다.", tags = {"App"})
    @PostMapping("/setVersion/android")
    public ResponseDto<?> setAndroidForceUpdateVersion(@UserId Long userId,
                                                       @Valid @RequestBody int updateVersion) {
        int updatedVersion = appService.setAndroidForceUpdateVersion(userId, updateVersion);
        return ResponseDto.ok(updatedVersion);
    }
}
