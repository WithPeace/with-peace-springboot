package com.example.withpeace.repository;

import com.example.withpeace.domain.User;
import com.example.withpeace.domain.UserInteraction;
import com.example.withpeace.type.EActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    List<UserInteraction> findByUserOrderByActionTimeDesc(User user);

    UserInteraction findByUserAndPolicyIdAndActionType(User user, String policyId, EActionType favorite);
}
