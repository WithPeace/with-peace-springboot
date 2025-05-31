package com.example.withpeace.controller;

import com.example.withpeace.annotation.UserId;
import com.example.withpeace.dto.ResponseDto;
import com.example.withpeace.dto.request.CommentRegisterRequestDto;
import com.example.withpeace.dto.request.PostRegisterRequestDto;
import com.example.withpeace.dto.request.ReportRegisterRequestDto;
import com.example.withpeace.dto.response.PostDetailResponseDto;
import com.example.withpeace.dto.response.PostRegisterResponseDto;
import com.example.withpeace.service.PostService;
import com.example.withpeace.type.ETopic;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "게시글 등록", description = "새로운 게시글을 등록합니다. 이미지 파일은 선택사항입니다.", tags = {"Post"})
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<PostRegisterResponseDto> registerPost(@UserId Long userId,
                                                             @Valid @ModelAttribute("postRegisterRequest") PostRegisterRequestDto postRegisterRequestDto,
                                                             @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {
        List<MultipartFile> file = Optional.ofNullable(imageFiles).orElse(Collections.emptyList());
        Long postId = postService.registerPost(userId, postRegisterRequestDto, file);
        return ResponseDto.ok(new PostRegisterResponseDto(postId));
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.", tags = {"Post"})
    @GetMapping("/{postId}")
    public ResponseDto<PostDetailResponseDto> getPostDetail(@UserId Long userId, @PathVariable Long postId) {
        return ResponseDto.ok(postService.getPostDetail(userId, postId));
    }

    @Operation(summary = "게시글 리스트 조회", description = "게시글 리스트를 페이징하여 조회합니다. 게시글 타입별로 필터링됩니다.", tags = {"Post"})
    @GetMapping("")
    public ResponseDto<?> getPostList(@UserId Long userId,
                                      @RequestParam ETopic type,
                                      @RequestParam(defaultValue = "0") @Valid @NotNull @Min(0) Integer pageIndex,
                                      @RequestParam(defaultValue = "1") @Valid @NotNull @Min(1) Integer pageSize) {
        return ResponseDto.ok(postService.getPostList(userId, type, pageIndex, pageSize));
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글의 내용을 수정합니다. 이미지도 수정 가능합니다.", tags = {"Post"})
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<PostRegisterResponseDto> updatePost(@UserId Long userId,
                                                             @PathVariable Long postId,
                                                             @Valid @ModelAttribute("postUpdateRequest") PostRegisterRequestDto postRegisterRequestDto,
                                                             @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {
        List<MultipartFile> file = Optional.ofNullable(imageFiles).orElse(Collections.emptyList());
        postService.updatePost(userId, postId, postRegisterRequestDto, file);
        return ResponseDto.ok(new PostRegisterResponseDto(postId));
    }

    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다.", tags = {"Post"})
    @DeleteMapping("/{postId}")
    public ResponseDto<?> deletePost(@UserId Long userId, @PathVariable Long postId) {
        return ResponseDto.ok(postService.deletePost(userId, postId));
    }

    @Operation(summary = "게시글 신고", description = "부적절한 게시글을 신고합니다. 신고 이유를 선택합니다.", tags = {"Post"})
    @PostMapping("/{postId}/reportPost")
    public ResponseDto<?> reportPost(@UserId Long userId, @PathVariable Long postId,
                                     @Valid @RequestBody ReportRegisterRequestDto reason) {
        return ResponseDto.ok(postService.reportPost(userId, postId, reason.reason()));
    }

    @Operation(summary = "댓글 등록 (v1)", description = "특정 게시글에 댓글을 등록합니다.", tags = {"Post"})
    @PostMapping("/{postId}/comments/register")
    public ResponseDto<?> registerComment(@UserId Long userId, @PathVariable Long postId,
                                          @Valid @RequestBody CommentRegisterRequestDto content) {
        return ResponseDto.ok(postService.registerComment(userId, postId, content.content()));
    }

    @Operation(summary = "댓글 신고 (v1)", description = "부적절한 댓글을 신고합니다.", tags = {"Post"})
    @PostMapping("/{commentId}/reportComment")
    public ResponseDto<?> reportComment(@UserId Long userId, @PathVariable Long commentId,
                                     @Valid @RequestBody ReportRegisterRequestDto reason) {
        return ResponseDto.ok(postService.reportComment(userId, commentId, reason.reason()));
    }

    @Operation(summary = "자주보는 게시판 조회", description = "각 주제별 최신 게시글을 하나씩 조회합니다.", tags = {"Post"})
    @GetMapping("/recents")
    public ResponseDto<?> getRecentPostList(@UserId Long userId) {
        return ResponseDto.ok(postService.getRecentPostList(userId));
    }
}
