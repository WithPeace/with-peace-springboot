package com.example.withpeace.repository;

import com.example.withpeace.domain.BalanceGame;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BalanceGameRepository extends JpaRepository<BalanceGame, Long> {

    @Query("SELECT bg FROM BalanceGame bg WHERE bg.gameDate <= :today ORDER BY bg.gameDate DESC")
    Page<BalanceGame> findByGameDateLessThanEqual(LocalDate today, Pageable pageable);

    boolean existsByGameDateLessThan(LocalDate gameDate);
    boolean existsByGameDateGreaterThan(LocalDate gameDate);

}
