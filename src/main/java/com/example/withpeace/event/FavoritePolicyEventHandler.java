package com.example.withpeace.event;

import com.example.withpeace.domain.FavoritePolicy;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.FavoritePolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FavoritePolicyEventHandler {

    private final FavoritePolicyRepository favoritePolicyRepository;

    @Async
    @EventListener
    public void handleFavoritePolicySave(FavoritePolicySaveEvent event) {
        try {
            favoritePolicyRepository.save(FavoritePolicy.of(event.user(), event.policy()));
        } catch (DataIntegrityViolationException e) {
            log.warn("이미 찜한 정책입니다. userId={}, policyId={}", event.user().getId(), event.policy().getId());
        } catch (Exception e) {
            throw new CommonException(ErrorCode.FAVORITE_YOUTH_POLICY_ERROR);
        }
    }
}
