package com.example.withpeace.repository;

import com.example.withpeace.domain.User;
import com.example.withpeace.domain.UserInteraction;
import com.example.withpeace.type.EActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

    // 사용자 조회 기록 저장 (조회 기록이 없으면 INSERT, 있으면 UPDATE)
    @Modifying
    @Query(value = "INSERT INTO user_interactions (user_id, policy_id, action_type, action_time) " +
            "VALUES (:userId, :policyId, :actionType, NOW())" +
            "ON DUPLICATE KEY UPDATE action_time = NOW()", nativeQuery = true)
    void upsertUserInteraction(Long userId, String policyId, String actionType);

    List<UserInteraction> findAllByUserOrderByActionTimeDesc(User user);

    UserInteraction findByUserIdAndPolicyIdAndActionType(Long userId, String policyId, EActionType actionType);

}
