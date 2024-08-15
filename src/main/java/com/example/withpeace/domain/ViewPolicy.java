package com.example.withpeace.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "view_policies")
public class ViewPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "policy_id", nullable = false, unique = true)
    private String policyId;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Builder
    public ViewPolicy(String policyId, int viewCount) {
        this.policyId = policyId;
        this.viewCount = viewCount;
    }

}
