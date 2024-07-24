package com.example.withpeace.repository;

import com.example.withpeace.domain.FavoritePolicy;
import com.example.withpeace.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoritePolicyRepository extends JpaRepository<FavoritePolicy, Long> {

    FavoritePolicy findByUserAndPolicyId(User user, String policyId);

    List<FavoritePolicy> findByUserOrderByCreateDateDesc(User user);

    boolean existsByUserAndPolicyId(User user, String policyId);
}
