package com.example.withpeace.service;

import com.example.withpeace.domain.BalanceGame;
import com.example.withpeace.domain.BalanceGameChoice;
import com.example.withpeace.domain.Comment;
import com.example.withpeace.domain.User;
import com.example.withpeace.dto.response.BalanceGameCommentListResponseDto;
import com.example.withpeace.dto.response.BalanceGameResponseDto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.BalanceGameChoiceRepository;
import com.example.withpeace.repository.BalanceGameRepository;
import com.example.withpeace.repository.CommentRepository;
import com.example.withpeace.repository.UserRepository;
import com.example.withpeace.type.EChoice;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceGameService {

    private final UserRepository userRepository;
    private final BalanceGameRepository balanceGameRepository;
    private final BalanceGameChoiceRepository balanceGameChoiceRepository;
    private final CommentRepository commentRepository;

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
    }

    @Transactional
    public List<BalanceGameResponseDto> getBalanceGame(Long userId, Integer pageIndex, Integer pageSize) {
        // 사용자 존재 여부 확인
        User user = getUserById(userId);

        // PageRequest 생성
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize);

        // 밸런스게임 목록 조회 (오늘 날짜 기준으로 과거 및 오늘 게임만 조회)
        Page<BalanceGame> page = balanceGameRepository.findByGameDateLessThanEqual(LocalDate.now(), pageRequest);

        List<BalanceGame> games = page.getContent();
        if(games.isEmpty()) { return Collections.emptyList(); }

        // 게임 ID 목록 추출
        List<Long> gameIds = games.stream().map(BalanceGame::getId).toList();

        // 사용자 선택 정보 조회
        List<BalanceGameChoice> choices = balanceGameChoiceRepository.findByGameIds(gameIds);
        Map<Long, Map<Long, EChoice>> allChoicesByGame = choices.stream()
                .collect(Collectors.groupingBy(
                        choice -> choice.getGame().getId(),
                        Collectors.toMap(
                                choice -> choice.getUser().getId(),
                                BalanceGameChoice::getChoice
                        )
                ));

        // 게임 목록 변환
        return games.stream().map(game -> {
            // 댓글 목록 변환
            List<BalanceGameCommentListResponseDto> comments = getCommentsWithChoices(game, allChoicesByGame);

            return BalanceGameResponseDto.of(
                    game.getId(),
                    formatGameDate(game.getGameDate()),
                    game.getTitle(),
                    game.getOptionA(),
                    game.getOptionB(),
                    getUserChoice(allChoicesByGame, game.getId(), user.getId()),
                    isActive(game.getGameDate()),
                    balanceGameRepository.existsByGameDateLessThan(game.getGameDate()),
                    LocalDate.now().equals(game.getGameDate())
                            ? false : balanceGameRepository.existsByGameDateGreaterThan(game.getGameDate()),
                    comments);
        }).toList();
    }

    private List<BalanceGameCommentListResponseDto> getCommentsWithChoices(BalanceGame game, Map<Long, Map<Long, EChoice>> allChoicesByGame) {
        // 댓글 작성자 및 선택 정보 조회
        List<Comment> comments = commentRepository.findCommentsWithWriterByBalanceGame(game);
        Map<Long, EChoice> choicesForGame = allChoicesByGame.getOrDefault(game.getId(), Map.of());

        return comments.stream()
                .map(comment -> convertToDto(comment, choicesForGame))
                .toList();
    }

    private BalanceGameCommentListResponseDto convertToDto(Comment comment, Map<Long, EChoice> commentUserChoiceMap) {
        // 댓글 작성자의 선택 정보 조회
        EChoice userChoice = commentUserChoiceMap.getOrDefault(comment.getWriter().getId(), null);

        return new BalanceGameCommentListResponseDto(
                comment.getId(),
                comment.getWriter().getId(),
                comment.getWriter().getNickname(),
                comment.getWriter().getProfileImage(),
                comment.getContent(),
                userChoice,
                comment.getCreateDate().toString()
        );
    }

    private EChoice getUserChoice(Map<Long, Map<Long, EChoice>> allChoicesByGame, Long gameId, Long userId) {
        Map<Long, EChoice> choicesForGame = allChoicesByGame.get(gameId);
        return (choicesForGame != null) ? choicesForGame.get(userId) : null;
    }

    private String formatGameDate(LocalDate gameDate) {
        // 게임 날짜가 "오늘"일 경우
        if(LocalDate.now().equals(gameDate)) {
            return "오늘의 밸런스게임";
        }
        // 게임 날짜가 "과거"일 경우
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일 밸런스게임");
        return gameDate.format(formatter);
    }

    private boolean isActive(LocalDate gameDate) {
        // 게임이 오늘 날짜이면 "활성", 아니면 "비활성"
        return LocalDate.now().equals(gameDate);
    }
}
