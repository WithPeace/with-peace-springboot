package com.example.withpeace.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "balance_games")
public class BalanceGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "option_a", nullable = false)
    private String optionA;

    @Column(name = "option_b", nullable = false)
    private String optionB;

    @Column(name = "game_date", nullable = false, unique = true)
    private LocalDate gameDate;

    @Builder
    public BalanceGame(String title, String optionA, String optionB, LocalDate gameDate) {
        this.title = title;
        this.optionA = optionA;
        this.optionB = optionB;
        this.gameDate = gameDate;
    }
}
