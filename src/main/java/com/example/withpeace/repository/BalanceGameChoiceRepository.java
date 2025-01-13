package com.example.withpeace.repository;

import com.example.withpeace.domain.BalanceGame;
import com.example.withpeace.domain.BalanceGameChoice;
import com.example.withpeace.domain.User;
import com.example.withpeace.type.EChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceGameChoiceRepository extends JpaRepository<BalanceGameChoice, Long> {

    @Query("SELECT bgc FROM BalanceGameChoice bgc WHERE bgc.game.id IN :gameIds")
    List<BalanceGameChoice> findByGameIds(List<Long> gameIds);

    boolean existsByUserAndGame(User user, BalanceGame game);

    long countByGameAndChoice(BalanceGame game, EChoice choice);
}
