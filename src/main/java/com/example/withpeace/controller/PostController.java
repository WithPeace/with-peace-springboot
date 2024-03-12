package com.example.withpeace.controller;

import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.dto.request.PostRegisterRequestDto;
import com.example.withpeace.dto.response.PostRegisterResponseDto;
import com.example.withpeace.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    // 게시글 등록
    @PostMapping("/register")
    public ResponseDto<PostRegisterResponseDto> registerPost(@UserId Long userId, @RequestBody PostRegisterRequestDto postRegisterRequestDto) {
        Long postId = postService.registerPost(userId, postRegisterRequestDto);
        return ResponseDto.ok(new PostRegisterResponseDto(postId));
    }
}
