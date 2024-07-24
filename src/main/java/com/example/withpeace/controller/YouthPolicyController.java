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

    // 정책 리스트 조회
    @GetMapping("")
    public ResponseDto<?> getPolicyList(@UserId Long userId,
                                        @RequestParam(defaultValue = "") String region,
                                        @RequestParam(defaultValue = "") String classification,
                                        @RequestParam(defaultValue = "1") @Valid @NotNull @Min(1) Integer pageIndex,
                                        @RequestParam(defaultValue = "10") @Valid @NotNull @Min(10) Integer display) {
        List<PolicyListResponseDto> policyList = youthPolicyService.getPolicyList(
                userId, region, classification, pageIndex - 1, display);

        return ResponseDto.ok(policyList);
    }

    // 정책 상세 조회
    @GetMapping("/{policyId}")
    public ResponseDto<?> getPolicyDetail(@PathVariable String policyId) {
        return ResponseDto.ok(youthPolicyService.getPolicyDetail(policyId));
    }

    // 정책 찜하기
    @PostMapping("/{policyId}/favorites")
    public ResponseDto<?> registerFavoritePolicy(@UserId Long userId, @PathVariable String policyId) {
        youthPolicyService.registerFavoritePolicy(userId, policyId);
        return ResponseDto.ok(true);
    }

    // 내가 찜한 정책 조회
    @GetMapping("/favorites")
    public ResponseDto<?> getFavoritePolicy(@UserId Long userId) {
        return ResponseDto.ok(youthPolicyService.getFavoritePolicy(userId));
    }

    // 정책 찜하기 해제
    @DeleteMapping("/{policyId}/favorites")
    public  ResponseDto<?> deleteFavoritePolicy(@UserId Long userId, @PathVariable String policyId) {
        youthPolicyService.deleteFavoritePolicy(userId, policyId);
        return ResponseDto.ok(true);
    }

}
