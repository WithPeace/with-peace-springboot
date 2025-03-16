package com.example.withpeace.service;

import com.example.withpeace.config.LegalDongCodeCache;
import com.example.withpeace.domain.*;
import com.example.withpeace.dto.response.*;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.*;
import com.example.withpeace.type.EActionType;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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

    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final FavoritePolicyRepository favoritePolicyRepository;
    private final ViewPolicyRepository viewPolicyRepository;
    private final UserInteractionRepository userInteractionRepository;
    private final UserService userService;
    private final WebClient webClient;
    private final LegalDongCodeCache legalDongCodeCache;

    private Policy getPolicyById(String policyId) {
        return policyRepository.findById(policyId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_YOUTH_POLICY));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
    }

    private boolean isFavoritePolicy(User user, String policyId) {
        return favoritePolicyRepository.existsByUserAndPolicyId(user, policyId);
    }

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
                    toSave.add(newPolicy);
                } else {
                    // 기존 데이터가 존재하는 경우 내용 비교 후 업데이트 수행
                    boolean isUpdated = existingPolicy.updateAllFieldsFrom(newPolicy);
                    if (isUpdated) {
                        toUpdate.add(existingPolicy);
                    }
                }

                // 처리된 데이터는 기존 데이터 목록에서 제거
                existingPolicyMap.remove(newPolicy.getId());
            }

            // 기존 데이터 중 Open API에서 삭제된 정책을 삭제 리스트에 추가
            List<Policy> toDelete = new ArrayList<>(existingPolicyMap.values());

            // Insert + Update + Hard Delete
            synchronizeYouthPolicies(toSave, toUpdate, toDelete);
        } catch (Exception e) {
            log.error("Error while processing youth policies: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.YOUTH_POLICY_PROCESSING_ERROR);
        }
    }

    // 새로운 정책 데이터를 저장(Insert), 기존 데이터를 수정(Update), 삭제된 데이터를 제거(Delete)
    @Transactional
    private void synchronizeYouthPolicies(List<Policy> toSave, List<Policy> toUpdate, List<Policy> toDelete) {
        try {
            if (!toSave.isEmpty()) {
                policyRepository.saveAll(toSave);
            }
            if (!toUpdate.isEmpty()) {
                policyRepository.saveAll(toUpdate);
            }
            if (!toDelete.isEmpty()) {
                policyRepository.deleteAll(toDelete);
            }
        } catch (Exception e) {
            log.error("Error while saving youth policy data: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.YOUTH_POLICY_SAVE_ERROR);
        }
    }

    // ToDo: .map(EPolicyRegion::fromCode) 수정
    @Transactional
    public List<PolicyListResponseDto> getPolicyList(Long userId, String region, String classification, Integer pageIndex, Integer display) {
        User user = getUserById(userId);

        List<EPolicyRegion> regionList = null;
        if (StringUtils.isNotBlank(region)) { // null, 빈 문자열, 공백만 있는 문자열을 모두 처리
            regionList = Arrays.stream(region.split(","))
                    .map(EPolicyRegion::fromCode)
                    .collect(Collectors.toList());
        }

        List<EPolicyClassification> classificationList = null;
        if (StringUtils.isNotBlank(classification)) {
            classificationList = Arrays.stream(classification.split(","))
                    .map(EPolicyClassification::fromCode)
                    .collect(Collectors.toList());
        }

        Pageable pageable = PageRequest.of(pageIndex, display);
        Page<Policy> youthPolicyPage;

        if (regionList != null && classificationList != null) {
            youthPolicyPage = policyRepository.findByRegionInAndClassificationIn(regionList, classificationList, pageable);
        } else if (regionList != null) {
            youthPolicyPage = policyRepository.findByRegionIn(regionList, pageable);
        } else if (classificationList != null) {
            youthPolicyPage = policyRepository.findByClassificationIn(classificationList, pageable);
        } else {
            youthPolicyPage = policyRepository.findAll(pageable);
        }

        List<PolicyListResponseDto> policyListResponseDtos = youthPolicyPage.getContent().stream()
                .map(policy -> createPolicyListResponseDto(user, policy))
                .collect(Collectors.toList());

        return policyListResponseDtos;
    }

    @Transactional
    public PolicyDetailResponseDto getPolicyDetail(Long userId, String policyId) {
        User user = getUserById(userId);
        Policy policy = getPolicyById(policyId);
        boolean isFavorite = isFavoritePolicy(user, policy.getId());
        viewPolicyRepository.incrementViewCount(policyId);
        userInteractionRepository.save(UserInteraction.builder() // 사용자 상호작용 데이터 생성
                .user(user)
                .policyId(policy.getId())
                .actionType(EActionType.VIEW)
                .build());

        return PolicyDetailResponseDto.from(policy, isFavorite);
    }

    @Transactional
    public void registerFavoritePolicy(Long userId, String policyId) {
        User user = getUserById(userId);
        Policy policy = getPolicyById(policyId);

        try{
            // 찜하기 되어있지 않은 경우 찜하기 처리 수행
            if(!isFavoritePolicy(user, policyId)) {
                favoritePolicyRepository.save(FavoritePolicy.builder()
                        .policyId(policy.getId())
                        .user(user)
                        .title(policy.getTitle())
                        .build());

                // 사용자 상호작용 데이터 생성
                userInteractionRepository.save(UserInteraction.builder()
                        .user(user)
                        .policyId(policy.getId())
                        .actionType(EActionType.FAVORITE)
                        .build());
            }
        } catch (Exception e) {
            throw new CommonException(ErrorCode.FAVORITE_YOUTH_POLICY_ERROR);
        }
    }

    @Transactional
    public List<FavoritePolicyListResponseDto> getFavoritePolicy(Long userId) {
        User user = getUserById(userId);

        try{
            List<FavoritePolicy> favoritePolicies = favoritePolicyRepository.findByUserOrderByCreateDateDesc(user);
            List<FavoritePolicyListResponseDto> favoritePolicyListResponseDtos = new ArrayList<>();

            for(FavoritePolicy favoritePolicy : favoritePolicies) {
                Policy policy = policyRepository.findById(favoritePolicy.getPolicyId()).orElse(null);
                if(policy != null) {
                    // 해당 정책이 존재하는 경우
                    if(!favoritePolicy.isActive()) favoritePolicy.setIsActive(true);
                    FavoritePolicyListResponseDto responseDto =
                            FavoritePolicyListResponseDto.from(policy, favoritePolicy.isActive());
                    favoritePolicyListResponseDtos.add(responseDto);
                }
                else { // 해당 정책이 존재하지 않는 경우
                    if(favoritePolicy.isActive()) favoritePolicy.setIsActive(false);
                    FavoritePolicyListResponseDto responseDto =
                            FavoritePolicyListResponseDto.from(favoritePolicy);
                    favoritePolicyListResponseDtos.add(responseDto);
                }
            }

            return favoritePolicyListResponseDtos;

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new CommonException(ErrorCode.FAVORITE_YOUTH_POLICY_ERROR);
        }
    }

    @Transactional
    public void deleteFavoritePolicy(Long userId, String policyId) {
        User user = getUserById(userId);
        FavoritePolicy favoritePolicy = favoritePolicyRepository.findByUserAndPolicyId(user, policyId);

        try {
            // 찜하기 해제가 되어있지 않은 경우 찜하기 해제 처리 수행
            if(favoritePolicy != null) {
                favoritePolicyRepository.delete(favoritePolicy);

                UserInteraction interaction =
                        userInteractionRepository.findByUserAndPolicyIdAndActionType(user, policyId, EActionType.FAVORITE);
                if (interaction != null) userInteractionRepository.delete(interaction); // 찜하기 상호작용 데이터 삭제
            }
        } catch (Exception e) {
            throw new CommonException(ErrorCode.FAVORITE_YOUTH_POLICY_ERROR);
        }
    }

    @Transactional
    public List<PolicyListResponseDto> getRecommendationPolicyList(Long userId) {
        User user = getUserById(userId);
        List<PolicyListResponseDto> recommendationList = new ArrayList<>();

        List<EPolicyRegion> regionList = user.getRegions(); // 지역 필터링 리스트
        List<EPolicyClassification> classificationList = user.getClassifications(); // 정책분야 필터링 리스트:
        Map<String, Integer> policyWeights = calculateInteractionWeight(user); // 사용자별 가중치 계산

        if(!policyWeights.isEmpty()) { // 상호작용 데이터가 있는 경우
            // 가중치 높은 순으로 정렬 & 지역 필터링 적용 & 상위 6개의 정책 가져오기
            recommendationList =  policyWeights.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // 가중치 내림차순
                    .map(entry -> policyRepository.findById(entry.getKey())
                            .filter(policy -> applyRegionAndClassificationFilter(policy, regionList, classificationList)) // 지역 & 정책분야 필터링 적용
                            .map(policy -> createPolicyListResponseDto(user, policy)) // 필터링된 정책으로 PolicyListResponseDto 생성
                            .orElse(null)) // 정책 없으면 null 반환
                    .filter(Objects::nonNull) // null값 제거 (유효하지 않은 정책 필터링)
                    .limit(6) // 상위 6개 정책 선택
                    .collect(Collectors.toList());
        }

        // 정책이 6개 미만일 경우 "핫한 정책"으로 부족한 갯수를 채움
        if(recommendationList.size() < 6) {
            // recommendationList에 이미 포함된 정책 ID들을 추출
            Set<String> existingPolicyIds = recommendationList.stream()
                    .map(PolicyListResponseDto::id)
                    .collect(Collectors.toSet());

            // "핫한 정책" 리스트에서 기존에 포함된 정책을 제외한 정책들만 가져옴
            List<PolicyListResponseDto> hotPolicyList = getHotPolicyList(user, regionList, classificationList, 12).stream()
                    .filter(policy -> !existingPolicyIds.contains(policy.id())) // 기존 정책과 중복되지 않는 것만 선택
                    .collect(Collectors.toList());

            // 두 리스트를 합친 후, 6개의 정책을 반환
            recommendationList = Stream.concat(recommendationList.stream(), hotPolicyList.stream())
                    .limit(6)
                    .collect(Collectors.toList());
        }

        return recommendationList;
    }

    private boolean applyRegionAndClassificationFilter(Policy policy, List<EPolicyRegion> regionList, List<EPolicyClassification> classificationList) {
        // 정책이 지역 및 정책분야 필터 조건을 만족하는지 확인
        return (regionList.isEmpty() || regionList.contains(policy.getRegion()))
                && (classificationList.isEmpty() || classificationList.contains(policy.getClassification()));
    }

    private PolicyListResponseDto createPolicyListResponseDto(User user, Policy policy) {
        boolean isFavorite = isFavoritePolicy(user, policy.getId()); // 찜하기 여부
        return PolicyListResponseDto.from(policy, isFavorite);
    }

    private Map<String, Integer> calculateInteractionWeight(User user) {
        List<UserInteraction> interactions = userInteractionRepository.findByUserOrderByActionTimeDesc(user);
        Map<String, Integer> policyWeights = new HashMap<>(); // 정책별 가중치 저장

        for(UserInteraction interaction : interactions) {
            String policyId = interaction.getPolicyId();
            EActionType actionType = interaction.getActionType();
            LocalDateTime actionTime = interaction.getActionTime();

            int weight = policyWeights.getOrDefault(policyId, 0); // 누적된 가중치 가져옴
            if(actionType == EActionType.VIEW) { // 조회
                weight += 1;
            } else if(actionType == EActionType.FAVORITE) { // 찜하기
                weight += 3;
            }
            // 최근 상호작용에 대한 가중치 1 추가 (최근 1주 이내)
            if(actionTime.isAfter(LocalDateTime.now().minusWeeks(1))) {
                weight += 1;
            }
            
            policyWeights.put(policyId, weight); // 정책별 가중치 정보 갱신
        }

        return policyWeights;
    }

    public List<PolicyListResponseDto> getHotPolicyList(User user, List<EPolicyRegion> regionList,
                                                         List<EPolicyClassification> classificationList, int count) {
        List<Policy> hotPolicyList = policyRepository.findHotPolicies();

        return hotPolicyList.stream()
                .filter(policy -> applyRegionAndClassificationFilter(policy, regionList, classificationList)) // 지역 & 정책분야 필터링 적용
                .limit(count) // 필요한 정책 수만큼 가져옴
                .map(policy -> createPolicyListResponseDto(user, policy))
                .collect(Collectors.toList());
    }

    public List<PolicyListResponseDto> getHotPolicyList(Long userId) {
        User user = getUserById(userId);
        List<Policy> hotPolicyList = policyRepository.findHotPolicies();

        return hotPolicyList.stream()
                .map(policy -> createPolicyListResponseDto(user, policy))
                .limit(6)
                .collect(Collectors.toList());
    }

    public PolicySearchResponseDto getSearchPolicyList(Long userId, String keyword, Integer pageIndex, Integer pageSize) {
        // 사용자 존재 여부 확인
        User user = getUserById(userId);

        // 검색어 검증 (null 체크 및 최소 2자 이상)
        if(keyword == null || keyword.trim().length() < 2) {
            throw new CommonException(ErrorCode.INVALID_POLICY_SEARCH_KEYWORD);
        }

        // 최신순 정렬
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.ASC, "rnum"));
        // 동적 검색 조건 생성
        Specification<Policy> spec = createSearchSpecification(keyword);
        // 검색 실행
        Page<Policy> searchResult = policyRepository.findAll(spec, pageRequest);

        // 찜한 정책 확인
        Set<String> favoriteIds = getFavoritePolicyIds(user.getId());
        // 검색된 정책들을 DTO로 변환
        List<PolicyListResponseDto> policies = searchResult.getContent().stream()
                .map(policy -> PolicyListResponseDto.from(policy, favoriteIds.contains(policy.getId())))
                .toList();

        // 응답 DTO 생성 및 반환
        return PolicySearchResponseDto.of(policies, searchResult.getTotalElements());
    }

    // 동적 검색 조건 생성
    private Specification<Policy> createSearchSpecification(String keyword) {
        // SQL Injection 방지를 위한 특수문자 이스케이프 처리
        String escapedKeyword = keyword.trim().replaceAll("[%_\\\\]", "\\\\$0");

        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 전체 문구 검색 (title, introduce, applicationDetails 필드에서 검색)
            List<Predicate> fullKeywordPredicates = Arrays.asList(
                    builder.like(root.get("title"), "%" + escapedKeyword + "%"),
                    builder.like(root.get("introduce"), "%" + escapedKeyword + "%"),
                    builder.like(root.get("applicationDetails"), "%" + escapedKeyword + "%")
            );
            predicates.add(builder.or(fullKeywordPredicates.toArray(new Predicate[fullKeywordPredicates.size()])));

            // 2. 공백으로 분리된 키워드별 검색 (키워드가 여러 개일 경우)
            String[] keywords = escapedKeyword.split("\\s+");
            if(keywords.length > 1) { // 여러 개의 키워드가 있을 때
                List<Predicate> keywordPredicates = Arrays.stream(keywords)
                        .map(kw -> Arrays.asList(
                                builder.like(root.get("title"), "%" + kw + "%"),
                                builder.like(root.get("introduce"), "%" + kw + "%"),
                                builder.like(root.get("applicationDetails"), "%" + kw + "%")
                        ))
                        .map(fieldPredicates -> builder.or(fieldPredicates.toArray(new Predicate[0])))
                        .collect(Collectors.toList());
                // 모든 키워드가 하나 이상의 필드에 포함되어야 함 (AND 조건)
                predicates.add(builder.and(keywordPredicates.toArray(new Predicate[0])));
            }

            // 전체 문구 검색 결과 OR 키워드별 검색 결과
            return builder.or(predicates.toArray(new Predicate[0]));
        };
    }

    // 사용자가 찜한 정책 ID 목록 조회
    private Set<String> getFavoritePolicyIds(Long userId) {
        // 사용자가 찜한 정책 목록을 조회하여 정책 ID만 Set으로 변환
        return favoritePolicyRepository.findByUserId(userId)
                .stream()
                .map(FavoritePolicy::getPolicyId)
                .collect(Collectors.toSet());
    }

}
