package com.example.withpeace.repository;

import com.example.withpeace.domain.ViewPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewPolicyRepository extends JpaRepository<ViewPolicy, Long> {

    // 조회수가 존재하면 증가시키는 UPDATE 쿼리 (반환값: 업데이트된 행 개수)
    @Modifying
    @Query(value = "UPDATE view_policies SET view_count = view_count + 1 " +
            "WHERE policy_id = :policyId", nativeQuery = true)
    int updateViewCount(String policyId);

    // 조회수가 없을 경우 새로운 조회수 INSERT
    @Modifying
    @Query(value = "INSERT INTO view_policies (policy_id, view_count) " +
            "VALUES (:policyId, 1)", nativeQuery = true)
    void insertViewCount(String policyId);

}