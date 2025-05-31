package com.example.withpeace.service;

import com.example.withpeace.component.EntityFinder;
import com.example.withpeace.dto.response.RecentPostResponseDto;
import com.example.withpeace.type.ECommentType;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.example.withpeace.domain.Comment;
import com.example.withpeace.domain.Image;
import com.example.withpeace.domain.User;
import com.example.withpeace.domain.Post;
import com.example.withpeace.domain.Report;
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
import com.example.withpeace.repository.ReportRepository;
import com.example.withpeace.type.EReason;
import com.example.withpeace.type.EReportType;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ImageRepository imageRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final Storage storage;
    private final EntityFinder entityFinder;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Transactional
    public Long registerPost(Long userId, PostRegisterRequestDto postRegisterRequestDto, List<MultipartFile> imageFiles) {
        User user = entityFinder.getUserById(userId);

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
        Post post = entityFinder.getPostById(postId);

        int idx = 0;
        for (MultipartFile file : imageFiles) {
            String fileName = idx + "_" + file.getOriginalFilename();
            String blobName = "postImage/" + postId + "/" + fileName;
            String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + blobName;

            try {
                BlobId blobId = BlobId.of(bucketName, blobName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                        .setContentType(file.getContentType())
                        .build();
                storage.create(blobInfo, file.getBytes());

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
        entityFinder.getUserById(userId);
        Post post = entityFinder.getPostById(postId);

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
                        .createDate(TimeFormatter.format(comment.getCreateDate()))
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
                        .createDate(TimeFormatter.format(post.getCreateDate()))
                        .postImageUrls(postImageUrls)
                        .comments(comments)
                        .build();

        post.incrementViewCount();

        return postDetailResponseDto;
    }

    @Transactional
    public List<PostListResponseDto> getPostList(Long userId, ETopic type, Integer pageIndex, Integer pageSize) {
        entityFinder.getUserById(userId);

        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<Post> postPage = postRepository.findByType(type, pageable);

        // 1. 모든 게시물 ID 수집
        List<Long> postIds = postPage.getContent().stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        // 2. 한 번의 쿼리로 모든 이미지 URL 조회
        Map<Long, String> postImageUrls = imageRepository.findFirstImageUrlsByPostIdsRaw(postIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0], // post_id를 key로
                        row -> (String) row[1] // url을 value로
                ));

        // 3. 정적 팩토리 메서드를 사용하여 DTO 생성
        List<PostListResponseDto> postListResponseDtos = postPage.getContent().stream()
                .map(post -> PostListResponseDto.from(post, postImageUrls.get(post.getId())))
                .collect(Collectors.toList());

        return postListResponseDtos;
    }

    @Transactional
    public Long updatePost(Long userId, Long postId, PostRegisterRequestDto postRegisterRequestDto, List<MultipartFile> imageFiles) {
        entityFinder.getUserById(userId);
        Post post = entityFinder.getPostById(postId);

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
        entityFinder.getUserById(userId);
        Post post = entityFinder.getPostById(postId);

        try {
            // GCS 이미지 삭제
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
        List<String> imageUrls = imageRepository.findUrlsByPost(post);
        List<BlobId> blobIdsToDelete = new ArrayList<>();

        for (String imageUrl : imageUrls) {
            String blobName = imageUrl.substring(imageUrl.indexOf(bucketName) + bucketName.length() + 1);
            blobIdsToDelete.add(BlobId.of(bucketName, blobName));
        }

        if (!blobIdsToDelete.isEmpty()) {
            storage.delete(blobIdsToDelete);
        }
    }

    @Transactional
    public Boolean reportPost(Long userId, Long postId, EReason reason) {
        User user = entityFinder.getUserById(userId);
        Post post = entityFinder.getPostById(postId);

        // 해당 게시글 중복 신고 확인
        boolean alreadyReported = reportRepository.existsByWriterAndPostAndType(user, post, EReportType.POST);
        if(alreadyReported) { throw new CommonException(ErrorCode.POST_ALREADY_REPORTED);}

        try {
            reportRepository.save(Report.builder()
                    .writer(user)
                    .post(post)
                    .type(EReportType.POST)
                    .reason(reason)
                    .build());

            return true;
        } catch (Exception e) {
            throw new CommonException(ErrorCode.POST_ERROR);
        }
    }

    @Transactional
    public Boolean registerComment(Long userId, Long postId, String content) {
        User user = entityFinder.getUserById(userId);
        Post post = entityFinder.getPostById(postId);

        try {
            commentRepository.save(Comment.builder()
                    .post(post)
                    .type(ECommentType.POST)
                    .writer(user)
                    .content(content)
                    .build());
            post.increaseCommentCount();

            return true;
        } catch (Exception e) {
            throw new CommonException(ErrorCode.POST_ERROR);
        }

    }

    @Transactional
    public Boolean reportComment(Long userId, Long commentId, EReason reason) {
        User user = entityFinder.getUserById(userId);
        Comment comment = entityFinder.getCommentById(commentId);

        // 해당 댓글 중복 신고 확인
        boolean alreadyReported = reportRepository.existsByWriterAndCommentAndType(user, comment, EReportType.COMMENT);
        if(alreadyReported) { throw new CommonException(ErrorCode.COMMENT_ALREADY_REPORTED);}

        try {
            reportRepository.save(Report.builder()
                    .writer(user)
                    .comment(comment)
                    .type(EReportType.COMMENT)
                    .reason(reason)
                    .build());

            return true;
        } catch (Exception e) {
            throw new CommonException(ErrorCode.POST_ERROR);
        }
    }

    public List<RecentPostResponseDto> getRecentPostList(Long userId) {
        List<Post> recentPostList = postRepository.findRecentPostsByType();

        return recentPostList.stream()
                .map(RecentPostResponseDto::from)
                .collect(Collectors.toList());
    }

}
