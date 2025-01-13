package com.example.withpeace.repository;

import com.example.withpeace.domain.BalanceGameChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceGameChoiceRepository extends JpaRepository<BalanceGameChoice, Long> {

    @Query("SELECT bgc FROM BalanceGameChoice bgc WHERE bgc.game.id IN :gameIds")
    List<BalanceGameChoice> findByGameIds(List<Long> gameIds);
}
