package com.example.withpeace.repository;

import com.example.withpeace.domain.YouthPolicy;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YouthPolicyRepository extends JpaRepository<YouthPolicy, Long> {

    Page<YouthPolicy> findByRegionInAndClassificationIn(List<EPolicyRegion> regions, List<EPolicyClassification> classifications, Pageable pageable);

    Page<YouthPolicy> findByRegionIn(List<EPolicyRegion> regions, Pageable pageable);

    Page<YouthPolicy> findByClassificationIn(List<EPolicyClassification> classifications, Pageable pageable);

}
