package com.example.withpeace.controller;

import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.dto.request.PostRegisterRequestDto;
import com.example.withpeace.dto.response.PostRegisterResponseDto;
import com.example.withpeace.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    // 게시글 등록
    @PostMapping("/register")
    public ResponseDto<PostRegisterResponseDto> registerPost(@UserId Long userId,
                                                             @Valid @ModelAttribute("postRegisterRequest") PostRegisterRequestDto postRegisterRequestDto,
                                                             @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {
        List<MultipartFile> file = Optional.ofNullable(imageFiles).orElse(Collections.emptyList());
        Long postId = postService.registerPost(userId, postRegisterRequestDto, file);
        return ResponseDto.ok(new PostRegisterResponseDto(postId));
    }
}
