package com.example.withpeace.repository;

import com.example.withpeace.domain.Policy;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, String>, JpaSpecificationExecutor<Policy> {

    // 지역 + 분야 조건으로 정책 페이지 조회
    @EntityGraph(attributePaths = {"region"})
    Page<Policy> findByRegionInAndClassificationIn(List<EPolicyRegion> regions, List<EPolicyClassification> classifications, Pageable pageable);

    // 지역 조건만으로 정책 페이지 조회
    @EntityGraph(attributePaths = {"region"})
    Page<Policy> findByRegionIn(List<EPolicyRegion> regions, Pageable pageable);

    // 분야 조건만으로 정책 페이지 조회
    @EntityGraph(attributePaths = {"region"})
    Page<Policy> findByClassificationIn(List<EPolicyClassification> classifications, Pageable pageable);

    // 정책 ID로 정책 조회
    @EntityGraph(attributePaths = {"region"})
    Optional<Policy> findById(String policyId);

    @Query("SELECT p FROM Policy p " +
            "LEFT JOIN ViewPolicy v ON p.id = v.policyId " +
            "LEFT JOIN FavoritePolicy f ON p.id = f.policyId " +
            "GROUP BY p.id, p.title, p.region, p.classification, f.id " +
            "ORDER BY (COALESCE(SUM(v.viewCount), 0) * 1 + COALESCE(COUNT(f.policyId), 0) * 3) DESC")
    List<Policy> findHotPolicies();

}
