package com.example.withpeace.service;

import com.example.withpeace.domain.AppVersion;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.AppVersionRepository;
import com.example.withpeace.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppService {

    private final UserRepository userRepository;
    private final AppVersionRepository appVersionRepository;

    private int androidForceUpdateVersion;

    @PostConstruct
    public void initVersion() {
        AppVersion appVersion = appVersionRepository.findFirstByOrderByIdAsc();
        if (appVersion != null) {
            androidForceUpdateVersion = appVersion.getAndroidForceUpdateVersion();
        } else {
            // 만약 상위 레코드가 없다면 새로운 레코드를 생성하여 1로 초기화한 후 저장합니다.
            AppVersion newAppVersion = appVersionRepository.save(AppVersion.builder().build());
            newAppVersion.setAndroidForceUpdateVersion(1);
            appVersionRepository.save(newAppVersion);
            androidForceUpdateVersion = 1;
        }
    }

    public boolean checkAndroidForceUpdate(int currentVersion) {
        return currentVersion < androidForceUpdateVersion;
    }

}
