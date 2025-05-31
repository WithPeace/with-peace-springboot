package com.example.withpeace.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "favorite_policies",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_policy", columnNames = {"user_id", "policy_id"}))
public class FavoritePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Builder
    public FavoritePolicy(Policy policy, User user, String title) {
        this.policy = policy;
        this.user = user;
        this.title = title;
        this.createDate = LocalDateTime.now();
    }

    public static FavoritePolicy of(User user, Policy policy) {
        return FavoritePolicy.builder()
                .policy(policy)
                .user(user)
                .title(policy.getTitle())
                .build();
    }
}
