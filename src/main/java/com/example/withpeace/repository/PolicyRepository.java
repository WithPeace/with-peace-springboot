package com.example.withpeace.repository;

import com.example.withpeace.domain.Policy;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long>, JpaSpecificationExecutor<Policy> {

    Page<Policy> findByRegionInAndClassificationIn(List<EPolicyRegion> regions, List<EPolicyClassification> classifications, Pageable pageable);

    Page<Policy> findByRegionIn(List<EPolicyRegion> regions, Pageable pageable);

    Page<Policy> findByClassificationIn(List<EPolicyClassification> classifications, Pageable pageable);

    Optional<Policy> findById(String policyId);

    @Query("SELECT p FROM Policy p " +
            "LEFT JOIN ViewPolicy v ON p.id = v.policyId " +
            "LEFT JOIN FavoritePolicy f ON p.id = f.policyId " +
            "GROUP BY p.id, p.title, p.region, p.classification, f.id " +
            "ORDER BY (COALESCE(SUM(v.viewCount), 0) * 1 + COALESCE(COUNT(f.policyId), 0) * 3) DESC")
    List<Policy> findHotPolicies();

}
