package com.example.withpeace.domain;


import com.example.withpeace.type.EProvider;
import com.example.withpeace.type.ERole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
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

    /* User Info */

    @Column(name = "email")
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Builder
    public User(String socialId, EProvider eProvider, ERole role) {
        this.socialId = socialId;
        this.eProvider = eProvider;
        this.role = role;
        this.createDate = LocalDate.now();
        this.isLogin = false;
        this.profileImage = "default.png";
    }

    public void register(String email, String nickname, String deviceToken) {
        this.email = email;
        this.nickname = nickname;
        this.deviceToken = deviceToken;
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
}
