package com.example.withpeace.controller;

import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.dto.response.PolicyListResponseDto;
import com.example.withpeace.dto.response.PolicySearchResponseDto;
import com.example.withpeace.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
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
public class PolicyController {

    private final PolicyService policyService;

    @Operation(summary = "정책 데이터 갱신 (관리자)",
            description = "청년 정책 데이터를 최신 정보로 갱신합니다. 관리자만 요청할 수 있습니다.", tags = {"Policy"})
    @PostMapping("/refresh")
    public ResponseDto<?> refreshPolicy(@UserId Long userId) {
        policyService.refreshYouthPolicies();
        return ResponseDto.ok(true);
    }

    @Operation(summary = "정책 목록 조회", description = "지역과 분류에 따른 정책 목록을 조회합니다.", tags = {"Policy"})
    @GetMapping("")
    public ResponseDto<?> getPolicyList(@UserId Long userId,
                                        @RequestParam(defaultValue = "") String region,
                                        @RequestParam(defaultValue = "") String classification,
                                        @RequestParam(defaultValue = "1") @Valid @NotNull @Min(1) Integer pageIndex,
                                        @RequestParam(defaultValue = "10") @Valid @NotNull @Min(10) @Max(50) Integer display) {
        List<PolicyListResponseDto> policyList = policyService.getPolicyList(
                userId, region, classification, pageIndex - 1, display);

        return ResponseDto.ok(policyList);
    }

    @Operation(summary = "정책 상세 조회", description = "특정 정책의 상세 정보를 조회합니다.", tags = {"Policy"})
    @GetMapping("/{policyId}")
    public ResponseDto<?> getPolicyDetail(@UserId Long userId, @PathVariable String policyId) {
        return ResponseDto.ok(policyService.getPolicyDetail(userId, policyId));
    }

    @Operation(summary = "정책 찜하기", description = "관심 있는 정책을 찜 목록에 추가합니다.", tags = {"Policy"})
    @PostMapping("/{policyId}/favorites")
    public ResponseDto<?> registerFavoritePolicy(@UserId Long userId, @PathVariable String policyId) {
        policyService.registerFavoritePolicy(userId, policyId);
        return ResponseDto.ok(true);
    }

    @Operation(summary = "찜한 정책 조회",
            description = "사용자가 찜한 모든 정책 목록을 조회합니다. 정책이 삭제되었더라도 찜 목록에는 표시됩니다.", tags = {"Policy"})
    @GetMapping("/favorites")
    public ResponseDto<?> getFavoritePolicy(@UserId Long userId) {
        return ResponseDto.ok(policyService.getFavoritePolicy(userId));
    }

    @Operation(summary = "정책 찜하기 취소", description = "찜한 정책을 찜 목록에서 제거합니다.", tags = {"Policy"})
    @DeleteMapping("/{policyId}/favorites")
    public  ResponseDto<?> deleteFavoritePolicy(@UserId Long userId, @PathVariable String policyId) {
        policyService.deleteFavoritePolicy(userId, policyId);
        return ResponseDto.ok(true);
    }

    @Operation(summary = "맞춤 정책 조회",
            description = "사용자의 지역/분야 필터와 상호작용(조회, 찜) 데이터를 기반으로 맞춤 정책을 추천합니다. ", tags = {"Policy"})
    @GetMapping("/recommendations")
    public ResponseDto<?> getRecommendationPolicyList(@UserId Long userId) {
        List<PolicyListResponseDto> recommendationPolicyList = policyService.getRecommendationPolicyList(userId);
        return ResponseDto.ok(recommendationPolicyList);
    }

    @Operation(summary = "핫한 정책 조회", description = "사용자 상호작용이 많은 상위 6개의 인기 정책을 조회합니다.", tags = {"Policy"})
    @GetMapping("/hot")
    public ResponseDto<?> getHotPolicyList(@UserId Long userId) {
        List<PolicyListResponseDto> hotPolicyList = policyService.getHotPolicyList(userId);
        return ResponseDto.ok(hotPolicyList);
    }

    @Operation(summary = "정책 검색", description = "키워드로 정책을 검색합니다.", tags = {"Policy"})
    @GetMapping("/search")
    public ResponseDto<?> getSearchPolicyList(@UserId Long userId,
                                    @RequestParam String keyword,
                                    @RequestParam(defaultValue = "1") @Valid @NotNull @Min(1) Integer pageIndex,
                                    @RequestParam(defaultValue = "10") @Valid @NotNull @Min(10) Integer pageSize) {
        PolicySearchResponseDto searchPolicyList = policyService.getSearchPolicyList(userId, keyword, pageIndex - 1, pageSize);
        return ResponseDto.ok(searchPolicyList);
    }

}
