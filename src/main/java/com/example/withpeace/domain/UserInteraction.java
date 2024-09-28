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
@Table(name = "user_interactions", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
public class UserInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "policy_id", nullable = false)
    private String policyId;

    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EActionType actionType;

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;

    @Builder

    public UserInteraction(User user, String policyId, EActionType actionType) {
        this.user = user;
        this.policyId = policyId;
        this.actionType = actionType;
        this.actionTime = LocalDateTime.now();
    }
}
