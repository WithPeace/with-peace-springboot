package com.example.withpeace.repository;

import com.example.withpeace.domain.YouthPolicy;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface YouthPolicyRepository extends JpaRepository<YouthPolicy, Long>, JpaSpecificationExecutor<YouthPolicy> {

    Page<YouthPolicy> findByRegionInAndClassificationIn(List<EPolicyRegion> regions, List<EPolicyClassification> classifications, Pageable pageable);

    Page<YouthPolicy> findByRegionIn(List<EPolicyRegion> regions, Pageable pageable);

    Page<YouthPolicy> findByClassificationIn(List<EPolicyClassification> classifications, Pageable pageable);

    Optional<YouthPolicy> findById(String policyId);

    @Query("SELECT p FROM YouthPolicy p " +
            "LEFT JOIN ViewPolicy v ON p.id = v.policyId " +
            "LEFT JOIN FavoritePolicy f ON p.id = f.policyId " +
            "GROUP BY p.rnum, p.id, p.title, p.region, p.classification, f.id " +
            "ORDER BY (COALESCE(SUM(v.viewCount), 0) * 1 + COALESCE(COUNT(f.policyId), 0) * 3) DESC")
    List<YouthPolicy> findHotPolicies();

}
