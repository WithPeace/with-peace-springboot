package com.example.withpeace.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.withpeace.domain.Image;
import com.example.withpeace.domain.User;
import com.example.withpeace.domain.Post;
import com.example.withpeace.dto.request.PostRegisterRequestDto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.ImageRepository;
import com.example.withpeace.repository.PostRepository;
import com.example.withpeace.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ImageRepository imageRepository;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public Long registerPost(Long userId, PostRegisterRequestDto postRegisterRequestDto) {
        User user =
                userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        Post post = postRepository.save(Post.builder()
                .writer(user)
                .title(postRegisterRequestDto.title())
                .content(postRegisterRequestDto.content())
                .type(postRegisterRequestDto.type())
                .build());

        // imageFiles가 비어있지 않은 경우에만 uploadImages 메소드를 호출합니다.
        if (postRegisterRequestDto.imageFiles() != null && !postRegisterRequestDto.imageFiles().isEmpty()) {
            uploadImages(post.getId(), postRegisterRequestDto.imageFiles());
        }

        return post.getId();
    }

    @Transactional
    private void uploadImages(Long postId, List<MultipartFile> imageFiles) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POST));

        for (MultipartFile file : imageFiles) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            String fileName = file.getOriginalFilename();
            String fileUrl = "https://" + bucket + "/postImage/" + postId + "/" + fileName;
            try {
                amazonS3.putObject(bucket, "postImage/" + postId + "/" + fileName, file.getInputStream(), metadata);
                Image image = imageRepository.save(Image.builder()
                        .post(post)
                        .url(fileUrl)
                        .build());
            } catch (Exception e) {
                throw new CommonException(ErrorCode.POST_FILE_UPLOAD_ERROR);
            }
        }

    }
}
