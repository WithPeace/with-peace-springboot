package com.example.withpeace.repository;

import com.example.withpeace.domain.FavoritePolicy;
import com.example.withpeace.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoritePolicyRepository extends JpaRepository<FavoritePolicy, Long> {

    @Query("SELECT f.policy.id FROM FavoritePolicy f WHERE f.user.id = :userId AND f.policy.id IN :policyIds")
    List<String> findFavoritePolicyIdsByUserIdAndPolicyIds(Long userId, List<String> policyIds);

    List<FavoritePolicy> findByUserOrderByCreateDateDesc(User user);

    boolean existsByUserAndPolicyId(User user, String policyId);

    List<FavoritePolicy> findByUserId(Long userId);
}
