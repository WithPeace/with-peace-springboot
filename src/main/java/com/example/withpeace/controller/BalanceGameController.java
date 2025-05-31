package com.example.withpeace.controller;

import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.dto.request.BalanceGameChoiceRequestDto;
import com.example.withpeace.service.BalanceGameService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/balance-games")
public class BalanceGameController {

    private final BalanceGameService balanceGameService;

    @Operation(summary = "밸런스게임 조회", description = "밸런스게임을 조회합니다. (초기: 오늘의 밸런스 게임)", tags = {"BalanceGame"})
    @GetMapping("")
    public ResponseDto<?> getBalanceGame(@UserId Long userId,
                                         @RequestParam(defaultValue = "0") @Valid @Min(0) Integer pageIndex,
                                         @RequestParam(defaultValue = "5") @Valid @Min(1) Integer pageSize) {
        return ResponseDto.ok(balanceGameService.getBalanceGame(userId, pageIndex, pageSize));
    }

    @Operation(summary = "밸런스게임 선택", description = "사용자가 특정 밸런스 게임에서 선택합니다.", tags = {"BalanceGame"})
    @PostMapping("/{gameId}/choices")
    public ResponseDto<?> selectBalanceGameChoice(@UserId Long userId, @PathVariable Long gameId,
                                                  @RequestBody @Valid BalanceGameChoiceRequestDto balanceGameChoiceRequestDto) {
        return ResponseDto.ok(balanceGameService.selectBalanceGameChoice(userId, gameId, balanceGameChoiceRequestDto.choice()));
    }
}
