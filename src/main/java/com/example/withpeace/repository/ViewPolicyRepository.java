package com.example.withpeace.repository;

import com.example.withpeace.domain.ViewPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ViewPolicyRepository extends JpaRepository<ViewPolicy, Long> {

    @Modifying
    @Query(value = "INSERT INTO view_policies (policy_id, view_count) VALUES (:policyId, 1) " +
            "ON DUPLICATE KEY UPDATE view_count = view_count + 1", nativeQuery = true)
    void incrementViewCount(String policyId);

}