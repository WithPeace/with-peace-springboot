package com.example.withpeace.controller;

import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.dto.request.CommentRegisterRequestV2Dto;
import com.example.withpeace.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 등록 (v2)", description = "특정 게시글에 댓글을 등록합니다.", tags = {"Post"})
    @PostMapping
    public ResponseDto<?> registerCommentV2(@UserId Long userId,
                                            @Valid @RequestBody CommentRegisterRequestV2Dto commentRegisterRequestV2Dto) {
        return ResponseDto.ok(commentService.registerCommentV2(userId, commentRegisterRequestV2Dto));
    }
}
