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
@Table(name = "appversions")
public class AppVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    @Column(name = "android_force_update_version", nullable = false)
    private int androidForceUpdateVersion;

    @Builder
    public AppVersion(int androidForceUpdateVersion) {
        this.androidForceUpdateVersion = androidForceUpdateVersion;
    }

    public void setAndroidForceUpdateVersion(int androidForceUpdateVersion) {
        this.androidForceUpdateVersion = androidForceUpdateVersion;
    }
}
