package com.example.withpeace.domain;

import com.example.withpeace.type.EActionType;
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
@Table(name = "user_interactions",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_policy_action", columnNames = {"user_id", "policy_id", "action_type"})
        }
)
public class UserInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Policy policy;

    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EActionType actionType;

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;

    @Builder
    public UserInteraction(User user, Policy policy, EActionType actionType) {
        this.user = user;
        this.policy = policy;
        this.actionType = actionType;
        this.actionTime = LocalDateTime.now();
    }
}
