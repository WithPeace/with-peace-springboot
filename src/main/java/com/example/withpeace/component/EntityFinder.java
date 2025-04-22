package com.example.withpeace.component;

import com.example.withpeace.domain.*;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EntityFinder {

    private final UserRepository userRepository;
    private final PolicyRepository policyRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final BalanceGameRepository balanceGameRepository;

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
    }

    public Policy getPolicyById(String policyId) {
        return policyRepository.findById(policyId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_YOUTH_POLICY));
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POST));
    }

    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_COMMENT));
    }

    public BalanceGame getBalanceGameById(Long gameId) {
        return balanceGameRepository.findById(gameId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_BALANCE_GAME));
    }

}
