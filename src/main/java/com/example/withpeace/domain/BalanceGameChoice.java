package com.example.withpeace.domain;

import com.example.withpeace.type.EChoice;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(
        name = "balance_game_choices",
        uniqueConstraints = {
                // 한 사용자가 동일한 게임에 여러 번 선택할 수 없음 (중복 선택 X)
                @UniqueConstraint(
                        name = "unique_game_user",
                        columnNames = {"game_id", "user_id"}
                )
        }
)
public class BalanceGameChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BalanceGame game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "choice", nullable = false)
    @Enumerated(EnumType.STRING)
    private EChoice choice;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Builder
    public BalanceGameChoice(BalanceGame game, User user, EChoice choice) {
        this.game = game;
        this.user = user;
        this.choice = choice;
        this.createDate = LocalDateTime.now();
    }
}
