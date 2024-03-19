package com.example.withpeace.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.withpeace.domain.Image;
import com.example.withpeace.domain.User;
import com.example.withpeace.domain.Post;
import com.example.withpeace.dto.request.PostRegisterRequestDto;
import com.example.withpeace.dto.response.PostDetailResponseDto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.ImageRepository;
import com.example.withpeace.repository.PostRepository;
import com.example.withpeace.repository.UserRepository;
import com.example.withpeace.util.TimeFormatter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ImageRepository imageRepository;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.s3.static}")
    private String endpoint;

    @Transactional
    public Long registerPost(Long userId, PostRegisterRequestDto postRegisterRequestDto, List<MultipartFile> imageFiles) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

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
        Post post = postRepository.findById(postId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POST));

        int idx = 0;
        for (MultipartFile file : imageFiles) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            String fileName = idx + "_" + file.getOriginalFilename();
            String fileUrl = endpoint + "/postImage/" + postId + "/" + fileName;
            try {
                amazonS3.putObject(bucket, "postImage/" + postId + "/" + fileName, file.getInputStream(), metadata);
                imageRepository.save(Image.builder()
                        .post(post)
                        .url(fileUrl)
                        .build());
            } catch (Exception e) {
                throw new CommonException(ErrorCode.POST_FILE_UPLOAD_ERROR);
            }

            idx++;
        }

    }

    @Transactional
    public PostDetailResponseDto getPostDetail(Long userId, Long postId) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        Post post =
                postRepository.findById(postId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POST));

        String formatterDate = TimeFormatter.timeFormat(post.getCreateDate());

        List<String> postImageUrls = Optional.ofNullable(imageRepository.findUrlsByPostId(post))
                                                .orElse(Collections.emptyList());


        PostDetailResponseDto postDetailResponseDto =
                PostDetailResponseDto.builder()
                        .postId(postId)
                        .userId(userId)
                        .nickname(user.getNickname())
                        .profileImageUrl(user.getProfileImage())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .type(post.getType())
                        .createDate(formatterDate)
                        .postImageUrls(postImageUrls)
                        .build();

        return postDetailResponseDto;
    }
}
