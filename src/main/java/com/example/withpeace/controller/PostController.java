package com.example.withpeace.controller;

import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.dto.request.PostRegisterRequestDto;
import com.example.withpeace.dto.response.PostDetailResponseDto;
import com.example.withpeace.dto.response.PostRegisterResponseDto;
import com.example.withpeace.service.PostService;
import com.example.withpeace.type.ETopic;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<PostRegisterResponseDto> registerPost(@UserId Long userId,
                                                             @Valid @ModelAttribute("postRegisterRequest") PostRegisterRequestDto postRegisterRequestDto,
                                                             @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {
        List<MultipartFile> file = Optional.ofNullable(imageFiles).orElse(Collections.emptyList());
        Long postId = postService.registerPost(userId, postRegisterRequestDto, file);
        return ResponseDto.ok(new PostRegisterResponseDto(postId));
    }

    // 게시글 상세조회
    @GetMapping("/{postId}")
    public ResponseDto<PostDetailResponseDto> getPostDetail(@UserId Long userId, @PathVariable Long postId) {
        return ResponseDto.ok(postService.getPostDetail(userId, postId));
    }

    // 게시글 리스트 조회
    @GetMapping("")
    public ResponseDto<?> getPostList(@UserId Long userId,
                                      @RequestParam ETopic type,
                                      @RequestParam(defaultValue = "0") @Valid @NotNull @Min(0) Integer pageIndex,
                                      @RequestParam(defaultValue = "1") @Valid @NotNull @Min(1) Integer pageSize) {
        return ResponseDto.ok(postService.getPostList(userId, type, pageIndex, pageSize));
    }
}
