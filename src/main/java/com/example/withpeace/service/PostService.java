package com.example.withpeace.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.withpeace.domain.Comment;
import com.example.withpeace.domain.Image;
import com.example.withpeace.domain.User;
import com.example.withpeace.domain.Post;
import com.example.withpeace.dto.request.PostRegisterRequestDto;
import com.example.withpeace.dto.response.CommentListResponseDto;
import com.example.withpeace.dto.response.PostDetailResponseDto;
import com.example.withpeace.dto.response.PostListResponseDto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.CommentRepository;
import com.example.withpeace.repository.ImageRepository;
import com.example.withpeace.repository.PostRepository;
import com.example.withpeace.repository.UserRepository;
import com.example.withpeace.type.ETopic;
import com.example.withpeace.util.TimeFormatter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ImageRepository imageRepository;
    private final CommentRepository commentRepository;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.s3.static}")
    private String endpoint;

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POST));
    }

    @Transactional
    public Long registerPost(Long userId, PostRegisterRequestDto postRegisterRequestDto, List<MultipartFile> imageFiles) {
        User user = getUserById(userId);

        Post post = postRepository.saveAndFlush(Post.builder()
                .writer(user)
                .title(postRegisterRequestDto.title())
                .content(postRegisterRequestDto.content())
                .type(postRegisterRequestDto.type())
                .build());

        // imageFiles가 비어있지 않은 경우에만 uploadImages 메소드를 호출합니다.
        if (imageFiles != null && !imageFiles.isEmpty()) {
            uploadImages(post.getId(), imageFiles);
        }

        return post.getId();
    }

    @Transactional
    private void uploadImages(Long postId, List<MultipartFile> imageFiles) {
        Post post = getPostById(postId);

        int idx = 0;
        for (MultipartFile file : imageFiles) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            String fileName = idx + "_" + file.getOriginalFilename();
            String fileUrl = endpoint + "/postImage/" + postId + "/" + fileName;
            try {
                amazonS3.putObject(bucket, fileUrl.substring(endpoint.length() + 1), file.getInputStream(), metadata);
                imageRepository.save(Image.builder()
                        .post(post)
                        .url(fileUrl)
                        .build());
            } catch (Exception e) {
                throw new CommonException(ErrorCode.POST_ERROR);
            }

            idx++;
        }

    }

    @Transactional
    public PostDetailResponseDto getPostDetail(Long userId, Long postId) {
        getUserById(userId);
        Post post = getPostById(postId);

        List<String> postImageUrls = Optional.ofNullable(imageRepository.findUrlsByPost(post))
                .orElse(Collections.emptyList());
        List<CommentListResponseDto> comments = Optional.ofNullable(commentRepository.findCommentsByPost(post))
                .orElse(Collections.emptyList())
                .stream()
                .map(comment -> CommentListResponseDto.builder()
                        .commentId(comment.getId())
                        .userId(comment.getWriter().getId())
                        .nickname(comment.getWriter().getNickname())
                        .profileImageUrl(comment.getWriter().getProfileImage())
                        .content(comment.getContent())
                        .createDate(TimeFormatter.timeFormat(comment.getCreateDate()))
                        .build())
                .collect(Collectors.toList());

        PostDetailResponseDto postDetailResponseDto =
                PostDetailResponseDto.builder()
                        .postId(postId)
                        .userId(post.getWriter().getId())
                        .nickname(post.getWriter().getNickname())
                        .profileImageUrl(post.getWriter().getProfileImage())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .type(post.getType())
                        .createDate(TimeFormatter.timeFormat(post.getCreateDate()))
                        .postImageUrls(postImageUrls)
                        .comments(comments)
                        .build();

        return postDetailResponseDto;
    }

    @Transactional
    public List<PostListResponseDto> getPostList(Long userId, ETopic type, Integer pageIndex, Integer pageSize) {
        getUserById(userId);

        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<Post> postPage = postRepository.findByType(type, pageable);

        List<PostListResponseDto> postListResponseDtos = postPage.getContent().stream()
                .map(post -> {
                    String postImageUrl = imageRepository.findUrlsByPostIdOrderByIdAsc(post.getId())
                            .orElse(null);
                    return PostListResponseDto.from(post, postImageUrl);
                })
                .collect(Collectors.toList());

        return postListResponseDtos;
    }

    @Transactional
    public Long updatePost(Long userId, Long postId, PostRegisterRequestDto postRegisterRequestDto, List<MultipartFile> imageFiles) {
        getUserById(userId);
        Post post = getPostById(postId);

        Boolean isExistDbImage = imageRepository.existsByPost(post); // DB 이미지 존재 여부

        try {
            post.setTitle(postRegisterRequestDto.title());
            post.setContent(postRegisterRequestDto.content());
            post.setType(postRegisterRequestDto.type());

            if(!imageFiles.isEmpty()) { // 이미지 수정 요청 O
                if(isExistDbImage) { // 기존 이미지 O
                    deleteImages(post); // 기존 이미지 모두 삭제
                    uploadImages(post.getId(), imageFiles); // 요청 이미지 모두 업로드
                } else { // 기존 이미지 X
                    uploadImages(post.getId(), imageFiles); // 요청 이미지 모두 업로드
                }
            } else if(imageFiles.isEmpty() && isExistDbImage) { // 이미지 수정 요청 X, 기존 이미지 O
                deleteImages(post); // 기존 이미지 모두 삭제
            }

            return post.getId();
        } catch (Exception e) {
            throw new CommonException(ErrorCode.POST_ERROR);
        }
    }

    @Transactional
    public Boolean deletePost(Long userId, Long postId) {
        getUserById(userId);
        Post post = getPostById(postId);

        try {
            // DB, S3 이미지 삭제
            deleteImages(post);

            // 게시물 삭제
            postRepository.delete(post);

            return true;
        } catch (Exception e) {
            throw new CommonException(ErrorCode.POST_ERROR);
        }
    }

    @Transactional
    private void deleteImages(Post post) {
        // DB에서 image 삭제
        imageRepository.deleteImagesByPost(post);

        // S3에서 image 삭제
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket);
        // 해당 경로로 시작하는 모든 객체를 삭제 대상에 추가함
        deleteObjectsRequest.withKeys("postImage/" + post.getId());
        amazonS3.deleteObjects(deleteObjectsRequest);
    }

    @Transactional
    public Boolean registerComment(Long userId, Long postId, String content) {
        User user = getUserById(userId);
        Post post = getPostById(postId);

        commentRepository.save(Comment.builder()
                .post(post)
                .writer(user)
                .content(content)
                .build());

        return true;
    }

}
