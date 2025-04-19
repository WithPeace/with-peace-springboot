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

    // 정책 리스트 필터링 조회 -- start
    // 지역 + 분야 조건으로 정책 페이지 조회
    @EntityGraph(attributePaths = {"region"})
    Page<Policy> findByRegionInAndClassificationIn(List<EPolicyRegion> regions, List<EPolicyClassification> classifications, Pageable pageable);

    // 지역 조건만으로 정책 페이지 조회
    @EntityGraph(attributePaths = {"region"})
    Page<Policy> findByRegionIn(List<EPolicyRegion> regions, Pageable pageable);

    // 분야 조건만으로 정책 페이지 조회
    @EntityGraph(attributePaths = {"region"})
    Page<Policy> findByClassificationIn(List<EPolicyClassification> classifications, Pageable pageable);
    // 정책 리스트 필터링 조회 -- end

    // 정책 ID로 정책 조회
    @EntityGraph(attributePaths = {"region"})
    Optional<Policy> findById(String policyId);

    // 핫한 정책 필터링 조회 -- start
    // 지역 + 분야 조건으로 필터링된 정책 중 조회수 + 찜수 기준 상위 limit개 조회
    @Query(value = "SELECT p.* FROM policies p " +
            "LEFT JOIN view_policies vp ON p.id = vp.policy_id " +
            "LEFT JOIN favorite_policies fp ON p.id = fp.policy_id " +
            "JOIN policy_regions pr ON pr.policy_id = p.id " +
            "WHERE pr.region IN (:regions) OR p.classification IN (:classifications) " +
            "GROUP BY p.id, p.sort_order " +
            "ORDER BY (COALESCE(SUM(vp.view_count), 0) + COUNT(fp.policy_id) * 3) DESC, p.sort_order ASC " +
            "LIMIT :limit", nativeQuery = true)
    List<Policy> findTopHotPoliciesByRegionsAndClassifications(List<EPolicyRegion> regions, List<EPolicyClassification> classifications, int limit);

    // 지역 조건만으로 필터링된 정책 중 조회수 + 찜수 기준 상위 limit개 조회
    @Query(value = "SELECT p.* FROM policies p " +
            "LEFT JOIN view_policies vp ON p.id = vp.policy_id " +
            "LEFT JOIN favorite_policies fp ON p.id = fp.policy_id " +
            "JOIN policy_regions pr ON pr.policy_id = p.id " +
            "WHERE pr.region IN (:regions) " +
            "GROUP BY p.id, p.sort_order " +
            "ORDER BY (COALESCE(SUM(vp.view_count), 0) + COUNT(fp.policy_id) * 3) DESC, p.sort_order ASC " +
            "LIMIT :limit", nativeQuery = true)
    List<Policy> findTopHotPoliciesByRegions(List<EPolicyRegion> regions, int limit);

    // 분야 조건만으로 필터링된 정책 중 조회수 + 찜수 기준 상위 limit개 조회
    @Query(value = "SELECT p.* FROM policies p " +
            "LEFT JOIN view_policies vp ON p.id = vp.policy_id " +
            "LEFT JOIN favorite_policies fp ON p.id = fp.policy_id " +
            "WHERE p.classification IN (:classifications) " +
            "GROUP BY p.id, p.sort_order " +
            "ORDER BY (COALESCE(SUM(vp.view_count), 0) + COUNT(fp.policy_id) * 3) DESC, p.sort_order ASC " +
            "LIMIT :limit", nativeQuery = true)
    List<Policy> findTopHotPoliciesByClassifications(List<EPolicyClassification> classifications, int limit);
    // 핫한 정책 필터링 조회 -- end

    // 핫한 정책 전체 조회
    // 필터 조건 없이 전체 정책 중 조회수 + 찜수 기준 상위 limit개 조회
    @Query(value = "SELECT p.* FROM policies p " +
            "LEFT JOIN view_policies vp ON p.id = vp.policy_id " +
            "LEFT JOIN favorite_policies fp ON p.id = fp.policy_id " +
            "GROUP BY p.id, p.sort_order " +
            "ORDER BY (COALESCE(SUM(vp.view_count), 0) * 1 + COUNT(fp.policy_id) * 3) DESC, p.sort_order ASC " +
            "LIMIT :limit", nativeQuery = true)
    List<Policy> findTopHotPolicies(int limit);

}
