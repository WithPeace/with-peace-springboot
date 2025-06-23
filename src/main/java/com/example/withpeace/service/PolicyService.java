package com.example.withpeace.service;

import com.example.withpeace.component.EntityFinder;
import com.example.withpeace.config.LegalDongCodeCache;
import com.example.withpeace.domain.*;
import com.example.withpeace.dto.response.*;
import com.example.withpeace.event.FavoritePolicySaveEvent;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.*;
import com.example.withpeace.type.EActionType;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    @Value("${youth-policy.api-key}")
    private String apiKeyNm;

    private static final int MAX_RECOMMENDATION_COUNT = 6;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final FavoritePolicyRepository favoritePolicyRepository;
    private final ViewPolicyRepository viewPolicyRepository;
    private final UserInteractionRepository userInteractionRepository;
    private final WebClient webClient;
    private final LegalDongCodeCache legalDongCodeCache;
    private final EntityFinder entityFinder;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00에 실행되도록 설정
    @Transactional
    public void refreshYouthPolicies(){
        try {
            log.info("Fetching youth policy data from external API...");

            // Open API에서 총 데이터 개수 조회
            Integer totalCount = fetchTotalPolicyCount();
            if(totalCount == 0) throw new CommonException(ErrorCode.YOUTH_POLICY_NO_DATA);

            // Open API에서 전체 정책 데이터 조회
            List<Policy> newPolicies = fetchAllPolicies(totalCount);

            // 기존 데이터와 비교하여 Insert + Update + Hard Delete 수행
            long beforeCount = policyRepository.count(); // 업데이트 전 개수
            processYouthPolicies(newPolicies);
            long afterCount = policyRepository.count(); // 업데이트 후 개수

            log.info("Youth policies successfully updated. Before: {}, After: {}", beforeCount, afterCount);

        } catch (Exception e) {
            log.error("Unexpected error in refreshYouthPolicies: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.YOUTH_POLICY_REFRESH_ERROR);
        }
    }

    // Open API에서 전체 데이터 개수 조회
    private Integer fetchTotalPolicyCount() {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("pageType", 2)
                            .queryParam("rtnType", "json")
                            .queryParam("apiKeyNm", apiKeyNm)
                            .queryParam("pageNum", 1)
                            .queryParam("pageSize", 0)
                            .build())
                    .retrieve()
                    .bodyToMono(YouthPolicyApiListResponseDto.class)
                    .map(dto -> dto.result().pagingInfo().totalCount())
                    .defaultIfEmpty(0) // 응답이 없을 경우 기본값 설정
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch total policy count from Open API: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.YOUTH_POLICY_TOTAL_COUNT_ERROR);
        }
    }

    // Open API에서 전체 정책 데이터 조회
    private List<Policy> fetchAllPolicies(Integer totalCount) {
        try {
            // 호출할 페이지 수 계산
            int pageSize = 50;
            int totalPageCount = (totalCount + pageSize  - 1) / pageSize;

            AtomicInteger sortOrder = new AtomicInteger(1); // 정렬 순서 관리

            // 정책 데이터 가져오기
            return Flux.range(1, totalPageCount)
                .delayElements(Duration.ofMillis(500)) // 요청 간 500ms 딜레이
                .concatMap(pageNum ->
                        webClient.get()
                                .uri(uriBuilder -> uriBuilder
                                        .queryParam("pageType", 2)
                                        .queryParam("rtnType", "json")
                                        .queryParam("apiKeyNm", apiKeyNm)
                                        .queryParam("pageNum", pageNum)
                                        .queryParam("pageSize", pageSize)
                                        .build())
                                .retrieve()
                                .bodyToMono(YouthPolicyApiListResponseDto.class)
                                .doOnError(error ->
                                        log.warn("API request failed, retrying... pageNum={}, error: {}", pageNum, error.getMessage()))
                                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))) // 최대 3번, 2초 간격으로 재시도
                                .flatMapIterable(dto ->
                                        Optional.ofNullable(dto.result())
                                                .map(YouthPolicyApiResultResponseDto::policyList)
                                                .orElse(Collections.emptyList())
                                )
                )
                .map(dto -> dto.toEntity(legalDongCodeCache, sortOrder.getAndIncrement())) // DTO -> Entity 변환 + 순서 부여
                .collectList() // List<Policy> 형태로 변환
                .block(); // 동기적으로 실행하여 전체 데이터 획득
    } catch (Exception e) {
            log.error("Failed to fetch policy data from Open API: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.YOUTH_POLICY_DATA_FETCH_ERROR);
        }
    }

    // 기존 정책 데이터와 비교하여 새로운 데이터를 Insert + Update + Delete 처리
    @Transactional
    private void processYouthPolicies(List<Policy> newPolicies) {
        try {
            // 기존 데이터 가져오기 (id -> Entity 매핑)
            Map<String, Policy> existingPolicyMap = policyRepository.findAll()
                    .stream().collect(Collectors.toMap(Policy::getId, Function.identity()));

            List<Policy> toSave = new ArrayList<>();
            List<Policy> toUpdate = new ArrayList<>();

            // 신규 데이터 저장 & 기존 데이터 업데이트
            for (int i=0; i<newPolicies.size(); i++) {
                Policy newPolicy = newPolicies.get(i);
                newPolicy.setSortOrder(i+1); // Open API 순서 반영

                Policy existingPolicy = existingPolicyMap.get(newPolicy.getId());

                if (existingPolicy == null) {
                    toSave.add(newPolicy); // INSERT 대상
                } else {
                    // 기존 데이터가 존재하는 경우 내용 비교 후 업데이트 수행
                    // 영속 상태 엔티티 필드 변경 → dirty checking 대상
                    existingPolicy.updateAllFieldsFrom(newPolicy);
                }

                // 처리된 데이터는 기존 데이터 목록에서 제거
                existingPolicyMap.remove(newPolicy.getId());
            }

            // 기존 데이터 중 Open API에서 삭제된 정책을 삭제 리스트에 추가
            List<Policy> toDelete = new ArrayList<>(existingPolicyMap.values());

            // Insert + Delete 처리
            synchronizeYouthPolicies(toSave, toDelete);
        } catch (Exception e) {
            log.error("Error while processing youth policies: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.YOUTH_POLICY_PROCESSING_ERROR);
        }
    }

    // 새로운 정책 데이터를 저장(Insert), 삭제된 데이터를 제거(Delete)
    @Transactional
    private void synchronizeYouthPolicies(List<Policy> toSave, List<Policy> toDelete) {
        try {
            if (!toSave.isEmpty()) {
                policyRepository.saveAll(toSave);
            }
            if (!toDelete.isEmpty()) {
                policyRepository.deleteAll(toDelete);
            }
        } catch (Exception e) {
            log.error("Error while saving youth policy data: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.YOUTH_POLICY_SAVE_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public List<PolicyListResponseDto> getPolicyList(Long userId, String region, String classification, Integer pageIndex, Integer display) {
        entityFinder.getUserById(userId); // 사용자 조회

        // 지역 필터링 (콤마(,)로 구분된 문자열을 변환)
        List<EPolicyRegion> regionList = null;
        if (StringUtils.isNotBlank(region)) { // null, 빈 문자열, 공백만 있는 문자열을 모두 처리
            regionList = Arrays.stream(region.split(","))
                    .map(EPolicyRegion::fromEnglishName)
                    .toList();
        }

        // 정책 분야 필터링 (콤마(,)로 구분된 문자열을 변환)
        List<EPolicyClassification> classificationList = null;
        if (StringUtils.isNotBlank(classification)) {
            classificationList = Arrays.stream(classification.split(","))
                    .map(EPolicyClassification::valueOf)
                    .toList();
        }

        // 페이지네이션 설정
        Pageable pageable = PageRequest.of(pageIndex, display, Sort.by(Sort.Direction.ASC, "sortOrder"));
        Page<Policy> policyPage;

        // 필터 조건에 따라 정책 조회
        if (regionList != null && classificationList != null) { // 지역 필터링 O & 정책분야 필터링 O
            policyPage = policyRepository.findByRegionInAndClassificationIn(regionList, classificationList, pageable);
        } else if (regionList != null) { // 지역 필터링 O & 정책분야 필터링 X
            policyPage = policyRepository.findByRegionIn(regionList, pageable);
        } else if (classificationList != null) { // 지역 필터링 X & 정책분야 필터링 O
            policyPage = policyRepository.findByClassificationIn(classificationList, pageable);
        } else {  // 지역 필터링 X & 정책분야 필터링 X
            policyPage = policyRepository.findAll(pageable);
        }

        // 사용자가 찜한 정책 ID 목록 조회
        Set<String> favoritePolicyIds = getFavoritePolicyIdsFromEntities(userId, policyPage.getContent());

        // DTO 변환 (사용자의 정책 찜하기 여부 포함)
        return policyPage.getContent().stream()
                .map(policy -> PolicyListResponseDto.from(policy, favoritePolicyIds.contains(policy.getId())))
                .toList();
    }

    // 사용자가 찜한 정책 ID 목록을 한 번의 쿼리로 조회 (N+1 문제 방지)
    private Set<String> getFavoritePolicyIdsFromEntities(Long userId, List<Policy> policies) {
        if (policies.isEmpty()) { return Collections.emptySet(); } // policy가 없을 경우 쿼리 실행 X
        
        List<String> policyIds = policies.stream()
                .map(Policy::getId)
                .toList();

        // FavoritePolicy 엔티티에서 user_id 와 policy_id를 기준으로 찜한 정책 조회 (policy id만 반환)
        List<String> favoritePolicyIds = favoritePolicyRepository.findFavoritePolicyIdsByUserIdAndPolicyIds(userId, policyIds);

        // Set<String> 으로 변환하여 빠르게 조회 가능하도록 설정
        return new HashSet<>(favoritePolicyIds);
    }

    @Transactional
    public PolicyDetailResponseDto getPolicyDetail(Long userId, String policyId) {
        entityFinder.getUserById(userId); // 사용자 조회
        Policy policy = entityFinder.getPolicyById(policyId); // 정책 조회

        // 사용자가 해당 정책을 찜했는지 여부 확인
        boolean isFavorite = favoritePolicyRepository.existsByUserIdAndPolicyId(userId, policyId);
        
        // 정책 조회수 증가 - 조회수가 존재하면 UPDATE, 존재하지 않으면 INSERT
        if (viewPolicyRepository.updateViewCount(policyId) == 0) {
            viewPolicyRepository.insertViewCount(policyId);
        }

        // 사용자 조회 기록 저장 - 조회 기록이 없으면 INSERT, 있으면 UPDATE
        userInteractionRepository.upsertUserInteraction(userId, policyId, EActionType.VIEW.name());

        // 정책 상세 정보 + 사용자의 찜 여부를 DTO 변환 및 반환
        return PolicyDetailResponseDto.from(policy, isFavorite);
    }

    @Transactional
    public void registerFavoritePolicy(Long userId, String policyId) {
        User user = entityFinder.getUserById(userId); // 사용자 조회
        Policy policy = entityFinder.getPolicyById(policyId); // 정책 조회

        // 찜 INSERT (트랜잭션 분리된 이벤트로 처리)
        applicationEventPublisher.publishEvent(new FavoritePolicySaveEvent(user, policy));

        try{
            // 사용자 상호작용 데이터 INSERT (이미 존재하면 action_time만 업데이트)
            userInteractionRepository.upsertUserInteraction(userId, policyId, EActionType.FAVORITE.name());
        } catch (Exception e) {
            throw new CommonException(ErrorCode.FAVORITE_YOUTH_POLICY_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public List<PolicyListResponseDto> getFavoritePolicy(Long userId) {
        entityFinder.getUserById(userId); // 사용자 조회

        // 사용자가 찜한 정책 목록 조회
        List<Policy> policies = favoritePolicyRepository.findPolicyByUserIdOrderByCreateDateDesc(userId);

        // DTO 변환 (찜한 정책이므로 찜하기 여부는 모두 true로 반환)
        return policies.stream()
                .map(policy -> PolicyListResponseDto.from(policy, true))
                .toList();
    }

    @Transactional
    public void deleteFavoritePolicy(Long userId, String policyId) {
        entityFinder.getUserById(userId); // 사용자 조회
        entityFinder.getPolicyById(policyId); // 정책 조회

        try {
            // 정책 찜하기 해제 (존재할 경우에만 삭제)
            FavoritePolicy favoritePolicy = favoritePolicyRepository.findByUserIdAndPolicyId(userId, policyId);
            if(favoritePolicy != null) favoritePolicyRepository.delete(favoritePolicy);

            // 사용자 상호작용 데이터 중 '찜하기" 유형 삭제
            UserInteraction interaction =
                    userInteractionRepository.findByUserIdAndPolicyIdAndActionType(userId, policyId, EActionType.FAVORITE);
            if (interaction != null) userInteractionRepository.delete(interaction);
        } catch (Exception e) {
            throw new CommonException(ErrorCode.FAVORITE_YOUTH_POLICY_ERROR);
        }
    }

    /**
     * 맞춤 정책 추천 API
     * - 사용자 상호작용 데이터를 기반으로 추천 정책을 계산
     * - 사용자 설정 지역/분야 필터를 적용하여 정책을 필터링
     * - 부족한 추천 개수는 핫한 정책으로 보완
     */
    @Transactional(readOnly = true)
    public List<PolicyListResponseDto> getRecommendationPolicyList(Long userId) {
        User user = entityFinder.getUserById(userId); // 사용자 조회

        // 사용자의 관심 지역 및 분야 필터 목록
        List<EPolicyRegion> regionList = user.getRegions(); // 지역 필터 리스트
        List<EPolicyClassification> classificationList = user.getClassifications(); // 정책분야 필터 리스트
        
        // 사용자 상호작용(조회, 찜하기) 기반 맞춤 정책 조회
        List<PolicyListResponseDto> recommendationList = getInteractionBasedRecommendations(user, regionList, classificationList);
        
        // 부족한 추천 정책 개수만큼 핫한 정책으로 보완
        fillWithHotPolicies(user, regionList, classificationList, recommendationList);

        return recommendationList;
    }

    /** 사용자 상호작용(조회, 찜하기) 기반 맞춤 정책 조회 **/
    private List<PolicyListResponseDto> getInteractionBasedRecommendations(User user,
                                                                           List<EPolicyRegion> regionList,
                                                                           List<EPolicyClassification> classificationList) {
        // 사용자 상호작용 데이터 기반 가중치 계산
        Map<String, Integer> policyWeights = calculatePolicyWeightByInteraction(user);
        if (policyWeights.isEmpty()) return new ArrayList<>();
        
        // 상호작용 가중치 높은 순으로 추천 정책 ID 추출
        List<String> recommendedPolicyIds = policyWeights.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        List<Policy> filteredRecommendedPolicies = policyRepository.findFilteredPoliciesByIdIn(
                recommendedPolicyIds,
                regionList, regionList.size(),
                classificationList, classificationList.size()
        ).stream().limit(MAX_RECOMMENDATION_COUNT).toList();

        // 사용자가 찜한 정책 ID 목록 조회
        Set<String> favoritePolicyIds = getFavoritePolicyIdsFromEntities(user.getId(), filteredRecommendedPolicies);

        // DTO 변환
        return filteredRecommendedPolicies.stream()
                .map(policy -> PolicyListResponseDto.from(policy, favoritePolicyIds.contains(policy.getId())))
                .collect(Collectors.toList());
    }

    /** 사용자 상호작용 데이터 기반 가중치 계산 **/
    private Map<String, Integer> calculatePolicyWeightByInteraction(User user) {
        // 사용자의 모든 상호작용 기록을 최신순으로 조회 (조회 & 찜 포함)
        List<UserInteraction> interactions = userInteractionRepository.findAllByUserOrderByActionTimeDesc(user);
        Map<String, Integer> policyWeights = new HashMap<>(); // 정책별 가중치 저장

        for(UserInteraction interaction : interactions) {
            String policyId = interaction.getPolicy().getId();
            EActionType actionType = interaction.getActionType();
            LocalDateTime actionTime = interaction.getActionTime();

            // 해당 정책의 현재 가중치 가져오기
            // 기존 가중치 없으면 0부터 시작
            int weight = policyWeights.getOrDefault(policyId, 0);

            // 상호작용 타입별 가중치 추가
            switch (actionType) {
                case VIEW -> weight += 1; // 조회 -> +1점
                case FAVORITE -> weight +=3; // 찜하기 -> +3점
            }

            // 최근 1주일 내 상호작용일 경우 가중치 1 추가
            if(actionTime.isAfter(LocalDateTime.now().minusWeeks(1))) {
                weight += 1;
            }

            policyWeights.put(policyId, weight);
        }

        return policyWeights;
    }

    /** 부족한 추천 정책 개수만큼 핫한 정책으로 보완 **/
    private void fillWithHotPolicies(User user,
                                     List<EPolicyRegion> regionList,
                                     List<EPolicyClassification> classificationList,
                                     List<PolicyListResponseDto> recommendationList) {
        // 최대 개수에서 현재 추천된 개수를 뺀 부족한 개수 계산
        int needed = MAX_RECOMMENDATION_COUNT - recommendationList.size();
        if (needed <= 0) return;

        // 이미 추천된 정책 ID들 (중복 방지용)
        Set<String> existingPolicyIds = recommendationList.stream()
                .map(PolicyListResponseDto::id)
                .collect(Collectors.toSet());

        // 필터 적용된 핫한 정책 → 전체 핫한 정책 순서로 조회
        List<PolicyListResponseDto> hotPolicyCandidates = Stream.of(
                        // 1차: 지역 및 분야 필터가 있는 경우 필터 적용된 핫한 정책 조회
                        (!regionList.isEmpty() || !classificationList.isEmpty())
                                ? getHotPolicyList(user, regionList, classificationList, needed * 3)
                                : List.<PolicyListResponseDto>of(),
                        // 2차: 전체 핫한 정책으로 보완
                        getHotPolicyList(user, List.of(), List.of(), needed * 3)
                )
                .flatMap(List::stream)
                .filter(dto -> !existingPolicyIds.contains(dto.id())) // 기존 상호작용 기반 추천과 핫한 정책 간 중복 제거
                .distinct() // 필터 적용 핫한 정책과 전체 핫한 정책 간 중복 제거 (1차/2차)
                .limit(needed) // 부족한 개수만큼 선택
                .toList();

        recommendationList.addAll(hotPolicyCandidates);
    }

    /** 핫한 정책 조회 및 필터링 **/
    public List<PolicyListResponseDto> getHotPolicyList(User user,
                                                        List<EPolicyRegion> regionList,
                                                         List<EPolicyClassification> classificationList,
                                                        int count) {
        List<Policy> hotPolicies;
        // 필터 조건에 따라 핫한 정책 조회
        if (!regionList.isEmpty() && !classificationList.isEmpty()) { // 지역 필터 O & 정책분야 필터 O
            hotPolicies = policyRepository.findTopHotPoliciesByRegionsAndClassifications(regionList, classificationList, count);
        } else if (!regionList.isEmpty()) { // 지역 필터 O & 정책분야 필터 X
            hotPolicies = policyRepository.findTopHotPoliciesByRegions(regionList, count);
        } else if (!classificationList.isEmpty()) { // 지역 필터 X & 정책분야 필터 O
            hotPolicies = policyRepository.findTopHotPoliciesByClassifications(classificationList, count);
        } else {  // 지역 필터 X & 정책분야 필터 X
            hotPolicies = policyRepository.findTopHotPolicies(count);
        }

        // 사용자 찜한 정책 ID 조회
        Set<String> favoritePolicyIds = getFavoritePolicyIdsFromEntities(user.getId(), hotPolicies);

        // Policy 엔티티를 DTO로 변환하여 반환
        return hotPolicies.stream()
                .map(policy -> PolicyListResponseDto.from(policy, favoritePolicyIds.contains(policy.getId())))
                .toList();
    }

    /**
     * 핫한 정책 조회 API
     * - Redis 캐시(JSON String) 사용
     * - 사용자 찜 여부만 실시간 반영
     * - 캐시 저장 시 DTO 리스트를 JSON 문자열로 저장하며, isFavorite 필드는 포함하지 않음
     */
    @Transactional(readOnly = true)
    public List<PolicyListResponseDto> getHotPolicyList(Long userId) {
        entityFinder.getUserById(userId); // 사용자 조회

        String key = "hot_policies";
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. Redis 캐시 조회
        String cachedJson = null;
        try {
            cachedJson = redisTemplate.opsForValue().get(key);
        } catch (DataAccessException e) {
            // Redis 연결 불가 시 DB fallback으로 전환되도록 경고 로그만 출력
            log.warn("Redis unavailable, fallback to DB: {}", e.getMessage());
        }
        if(cachedJson != null) {
            try {
                // JSON 문자열을 객체로 역직렬화
                List<PolicyCacheListResponseDto> cached = objectMapper.readValue(
                        cachedJson,
                        new TypeReference<>() {}
                );

                // 사용자 찜한 정책 ID 조회
                Set<String> favoritePolicyIds = getFavoritePolicyIdsFromDtos(userId, cached);

                // 캐시된 리스트에 찜 여부만 반영하여 반환
                return cached.stream()
                        .map(dto -> PolicyListResponseDto.builder()
                                .id(dto.id())
                                .title(dto.title())
                                .introduce(dto.introduce())
                                .classification(dto.classification())
                                .region(dto.region().stream().map(EPolicyRegion::valueOf).toList())
                                .ageInfo(dto.ageInfo())
                                .applicationPeriodStatus(dto.applicationPeriodStatus())
                                .isFavorite(favoritePolicyIds.contains(dto.id()))
                                .build())
                        .toList();
            } catch (Exception e) {
                log.warn("Failed to deserialize cached policies: {}", e.getMessage());
                redisTemplate.delete(key); // 캐시 역직렬화 실패 시 캐시 삭제
            }
        }

        // 2. 캐시 미스 -> DB에서 핫한 정책 조회
        // 조회수 + 찜수 기준 상위 6개의 정책만 조회
        List<Policy> hotPolicies = policyRepository.findTopHotPolicies(MAX_RECOMMENDATION_COUNT);
        // Lazy 필드 초기화 (native query + DTO 에서 사용 시 필요)
        hotPolicies.forEach(policy -> Hibernate.initialize(policy.getRegion()));
        // 사용자 찜한 정책 ID 조회
        Set<String> favoritePolicyIds = getFavoritePolicyIdsFromEntities(userId, hotPolicies);

        // 3. DTO 변환
        List<PolicyListResponseDto> result = hotPolicies.stream()
                .map(policy -> PolicyListResponseDto.from(policy, favoritePolicyIds.contains(policy.getId())))
                .toList();

        // 4. Redis 캐시 저장
        List<PolicyCacheListResponseDto> toCache = result.stream()
                .map(PolicyCacheListResponseDto::from)
                .toList();

        try {
            // 객체를 JSON 문자열로 직렬화하여 Redis에 저장
            String cacheValue = objectMapper.writeValueAsString(toCache);

            // TTL 1시간으로 설정하여 캐시 저장
            redisTemplate.opsForValue().set(key, cacheValue, Duration.ofHours(1));

            log.info("Successfully cached hot policies to Redis with key '{}'", key);
        } catch (Exception e) {
            // 직렬화 실패 시 로그만 남기고 캐싱은 생략 (기능 영향 없음)
            log.warn("Failed to serialize policies for caching: {}", e.getMessage());
        }

        return result;
    }

    /**
     * Redis 캐시에서 ID만 추출해 찜 여부 확인
     */
    private Set<String> getFavoritePolicyIdsFromDtos(Long userId, List<PolicyCacheListResponseDto> dtos) {
        if (dtos.isEmpty()) return Collections.emptySet();
        List<String> ids = dtos.stream().map(PolicyCacheListResponseDto::id).toList();
        List<String> favoriteIds = favoritePolicyRepository.findFavoritePolicyIdsByUserIdAndPolicyIds(userId, ids);
        return new HashSet<>(favoriteIds);
    }

    @Transactional(readOnly = true)
    public PolicySearchResponseDto getSearchPolicyList(Long userId, String keyword, Integer pageIndex, Integer pageSize) {
        entityFinder.getUserById(userId); // 사용자 조회

        // 검색어 유효성 검증 (null 체크 및 최소 2자 이상)
        if(keyword == null || keyword.trim().length() < 2) {
            throw new CommonException(ErrorCode.INVALID_POLICY_SEARCH_KEYWORD);
        }

        // 페이징 정보 및 정렬 기준 설정 (sortOrder 기준 오름차순)
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.ASC, "sortOrder"));

        // 키워드 기반 동적 검색 조건 생성
        Specification<Policy> searchSpec = createSearchSpecification(keyword);

        // 정책 검색 실행 (페이징 적용)
        Page<Policy> searchResultPage = policyRepository.findAll(searchSpec, pageRequest);
        List<Policy> searchResultList = searchResultPage.getContent();

        // 검색 결과 중 사용자 찜한 정책 ID 조회
        Set<String> favoritePolicyIds = getFavoritePolicyIdsFromEntities(userId, searchResultList);

        // DTO 변환
        List<PolicyListResponseDto> result = searchResultList.stream()
                .map(policy -> PolicyListResponseDto.from(policy, favoritePolicyIds.contains(policy.getId())))
                .toList();

        // 응답 DTO 반환 (정책 목록 + 전체 개수)
        return PolicySearchResponseDto.of(result, searchResultPage.getTotalElements());
    }

    // 동적 검색 조건 생성
    private Specification<Policy> createSearchSpecification(String keyword) {
        // SQL Injection 방지를 위한 검색어 전처리 (trim + 특수문자 이스케이프)
        String escapedKeyword = keyword.trim().replaceAll("[%_\\\\]", "\\\\$0");

        // 키워드가 공백으로 여러 개 나뉘는 경우
        String[] keywordArray = escapedKeyword.split("\\s+");

        return (root, query, builder) -> {
            // 검색 대상 필드 (title, introduce, applicationDetails 필드에서 검색)
            Path<String> title = root.get("title");
            Path<String> introduce = root.get("introduce");
            Path<String> applicationDetails = root.get("applicationDetails");

            // 1. 전체 문구 포함 조건
            Predicate fullTextMatch = builder.or(
                    builder.like(title, "%" + escapedKeyword + "%"),
                    builder.like(introduce, "%" + escapedKeyword + "%"),
                    builder.like(applicationDetails, "%" + escapedKeyword + "%")
            );

            // 2. 각 단어가 하나 이상 포함되는 AND 조건
            List<Predicate> wordPredicates = new ArrayList<>();
            for (String word : keywordArray) {
                Predicate wordMatch = builder.or(
                        builder.like(title, "%" + word + "%"),
                        builder.like(introduce, "%" + word + "%"),
                        builder.like(applicationDetails, "%" + word + "%")
                );
                wordPredicates.add(wordMatch);
            }
            Predicate allWordsMatch = builder.and(wordPredicates.toArray(new Predicate[0]));

            // 전체 문구 포함 OR 모든 단어 포함 조건
            return builder.or(fullTextMatch, allWordsMatch);
        };
    }

}
