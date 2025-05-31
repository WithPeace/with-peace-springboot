package com.example.withpeace.service;

import com.example.withpeace.component.EntityFinder;
import com.example.withpeace.domain.*;
import com.example.withpeace.dto.request.CommentRegisterRequestV2Dto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.*;
import com.example.withpeace.type.EReason;
import com.example.withpeace.type.EReportType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final EntityFinder entityFinder;

    @Transactional
    public boolean registerCommentV2(Long userId, CommentRegisterRequestV2Dto commentRegisterRequestV2Dto) {
        // 사용자 존재 여부 확인
        User user = entityFinder.getUserById(userId);

        // 댓글 엔티티 빌더 생성
        Comment.CommentBuilder commentBuilder = Comment.builder()
                .type(commentRegisterRequestV2Dto.targetType())
                .writer(user)
                .content(commentRegisterRequestV2Dto.content());

        // 대상 ID 존재 여부 확인 후 설정
        switch (commentRegisterRequestV2Dto.targetType()) {
            case POST -> commentBuilder.post(entityFinder.getPostById(commentRegisterRequestV2Dto.targetId()));
            case BALANCE_GAME -> commentBuilder.game(entityFinder.getBalanceGameById(commentRegisterRequestV2Dto.targetId()));
            default -> throw new CommonException(ErrorCode.INVALID_COMMENT_TYPE);
        }

        commentRepository.save(commentBuilder.build());
        return true;
    }

    @Transactional
    public boolean reportCommentV2(Long userId, Long commentId, EReason reason) {
        // 사용자 존재 여부 확인
        User user = entityFinder.getUserById(userId);
        // 댓글 존재 여부 확인
        Comment comment = entityFinder.getCommentById(commentId);

        // 해당 댓글 중복 신고 확인
        if(reportRepository.existsByWriterAndCommentAndType(user, comment, EReportType.COMMENT)) {
            throw new CommonException(ErrorCode.COMMENT_ALREADY_REPORTED);
        }

        reportRepository.save(Report.builder()
                .writer(user)
                .comment(comment)
                .type(EReportType.COMMENT)
                .reason(reason)
                .build());

        return true;
    }
}
