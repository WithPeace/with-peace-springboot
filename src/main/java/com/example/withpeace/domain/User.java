package com.example.withpeace.domain;


import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import com.example.withpeace.type.EProvider;
import com.example.withpeace.type.ERole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    @Column(name = "social_id", unique = true)
    private String socialId;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private EProvider eProvider;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ERole role;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "refresh_token")
    private String refreshToken;


    @Column(name = "is_login", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isLogin;

    @Column(name = "device_token")
    private String deviceToken;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "is_deleted", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "delete_date")
    private LocalDate deleteDate;

    /* User Info */

    @Column(name = "email")
    private String email;

    @Column(name = "nickname", unique = true)
    private String nickname;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_regions", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "region")
    private List<EPolicyRegion> regions;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_classifications", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "classification")
    private List<EPolicyClassification> classifications;

    @Builder
    public User(String socialId, EProvider eProvider, ERole role, String email) {
        this.socialId = socialId;
        this.eProvider = eProvider;
        this.role = role;
        this.createDate = LocalDate.now();
        this.isLogin = false;
        this.profileImage = "default.png";
        this.email = email;
    }

    public void updateNickname(String nickname) {
        if(nickname != this.nickname){
            this.nickname = nickname;
        }
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void setRole(ERole role) {
        this.role = role;
    }

    public void setLogin(Boolean login) {
        isLogin = login;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setDeleteDate() {
        this.deleteDate = LocalDate.now();
    }

    public void recoveryUser(){
        this.isDeleted = false;
        this.deleteDate = null;
    }

    public void setRegions(List<EPolicyRegion> regions) {
        this.regions = regions;
    }

    public void setClassifications(List<EPolicyClassification> classifications) {
        this.classifications = classifications;
    }
}
