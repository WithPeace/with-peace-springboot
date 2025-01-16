package com.example.withpeace.service;

import com.example.withpeace.domain.BalanceGame;
import com.example.withpeace.domain.Comment;
import com.example.withpeace.domain.Post;
import com.example.withpeace.domain.User;
import com.example.withpeace.dto.request.CommentRegisterRequestV2Dto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.BalanceGameRepository;
import com.example.withpeace.repository.CommentRepository;
import com.example.withpeace.repository.PostRepository;
import com.example.withpeace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final BalanceGameRepository balanceGameRepository;

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POST));
    }

    private BalanceGame getBalanceGameById(Long gameId) {
        return balanceGameRepository.findById(gameId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_BALANCE_GAME));
    }

    public boolean registerCommentV2(Long userId, CommentRegisterRequestV2Dto commentRegisterRequestV2Dto) {
        // 사용자 존재 여부 확인
        User user = getUserById(userId);

        // 댓글 엔티티 빌더 생성
        Comment.CommentBuilder commentBuilder = Comment.builder()
                .type(commentRegisterRequestV2Dto.targetType())
                .writer(user)
                .content(commentRegisterRequestV2Dto.content());

        // 대상 ID 존재 여부 확인 후 설정
        switch (commentRegisterRequestV2Dto.targetType()) {
            case POST -> commentBuilder.post(getPostById(commentRegisterRequestV2Dto.targetId()));
            case BALANCE_GAME -> commentBuilder.game(getBalanceGameById(commentRegisterRequestV2Dto.targetId()));
            default -> throw new CommonException(ErrorCode.INVALID_COMMENT_TYPE);
        }

        commentRepository.save(commentBuilder.build());
        return true;
    }
}
