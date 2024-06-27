package com.example.withpeace.controller;

import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.dto.response.PolicyListResponseDto;
import com.example.withpeace.service.YouthPolicyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/policies")
@Slf4j
public class YouthPolicyController {

    private final YouthPolicyService youthPolicyService;

    // 정책 데이터 리프레시 (관리자)
    @PostMapping("/refresh")
    public ResponseDto<?> refreshPolicy(@UserId Long userId) {
        youthPolicyService.scheduledFetchAndSaveYouthPolicy();
        return ResponseDto.ok(true);
    }
}
