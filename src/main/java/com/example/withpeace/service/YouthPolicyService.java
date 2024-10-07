package com.example.withpeace.service;

import com.example.withpeace.domain.*;
import com.example.withpeace.dto.response.*;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.*;
import com.example.withpeace.type.EActionType;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouthPolicyService {

    @Value("${youth-policy.api-key}")
    private String apiKey;

    private int saveCount = 0;

    private final YouthPolicyRepository youthPolicyRepository;
    private final UserRepository userRepository;
    private final FavoritePolicyRepository favoritePolicyRepository;
    private final ViewPolicyRepository viewPolicyRepository;
    private final UserInteractionRepository userInteractionRepository;
    private final UserService userService;

    private YouthPolicy getPolicyById(String policyId) {
        return youthPolicyRepository.findById(policyId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_YOUTH_POLICY));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
    }

    private boolean isFavoritePolicy(User user, String policyId) {
        return favoritePolicyRepository.existsByUserAndPolicyId(user, policyId);
    }

    @Scheduled(cron = "0 0 0 * * MON") // 매주 월요일 00:00에 실행되도록 설정
    @Transactional
    public void scheduledFetchAndSaveYouthPolicy() {
        try {
            // 데이터 삭제
            deleteAllYouthPolicies();
            saveCount = 0;

            // 데이터 가져오기 및 저장
            fetchAndSaveYouthPolicy();

            log.info("Youth Policy data update job completed. Total {} policies saved.", saveCount);
        } catch (CommonException e) {
            if (e.getErrorCode() == ErrorCode.YOUTH_POLICY_FETCH_AND_SAVE_ERROR
                || e.getErrorCode() == ErrorCode.YOUTH_POLICY_DELETE_ERROR)
                throw e;
        } catch (Exception e) {
            throw new CommonException(ErrorCode.YOUTH_POLICY_SCHEDULED_ERROR);
        }
    }

    @Transactional
    private void deleteAllYouthPolicies() {
        try {
            youthPolicyRepository.deleteAll();
            log.info("All existing youth policies deleted.");
        } catch (Exception e) {
            throw new CommonException(ErrorCode.YOUTH_POLICY_DELETE_ERROR);
        }
    }

    @Transactional
    private void fetchAndSaveYouthPolicy(){
        try {
            RestTemplate restTemplate = new RestTemplate();
            XmlMapper xmlMapper = new XmlMapper();

            String apiUrl = "https://www.youthcenter.go.kr/opi/youthPlcyList.do" +
                    "?openApiVlak=" + apiKey + "&pageIndex=1&display=1";

            // XML 파싱 & 전체 페이지 수 계산
            String firstPageResponse = restTemplate.getForObject(apiUrl, String.class);
            YouthPolicyListResponseDto firstPageData = xmlMapper.readValue(firstPageResponse, YouthPolicyListResponseDto.class);
            int totalCount = firstPageData.totalCount();
            int pageCount = (totalCount/100) + ((totalCount%100 == 0) ? 0 : 1);

            List<YouthPolicy> entities = new ArrayList<>();

            for(int pageIndex=1; pageIndex<=pageCount; pageIndex++){
                String pageUrl = "https://www.youthcenter.go.kr/opi/youthPlcyList.do" +
                        "?openApiVlak=" + apiKey + "&pageIndex=" + pageIndex + "&display=100";
                String pageResponse = restTemplate.getForObject(pageUrl, String.class);
                YouthPolicyListResponseDto pageData = xmlMapper.readValue(pageResponse, YouthPolicyListResponseDto.class);

                List<YouthPolicyResponseDto> policies = pageData.youthPolicyListResponseDto();
                entities.addAll(loadPolicies(policies));
            }
            youthPolicyRepository.saveAll(entities);
            saveCount = entities.size();

        } catch (Exception e) {
            throw new CommonException(ErrorCode.YOUTH_POLICY_FETCH_AND_SAVE_ERROR);
        }

    }

    @Transactional
    private List<YouthPolicy> loadPolicies(List<YouthPolicyResponseDto> policies) {
        List<YouthPolicy> entities = new ArrayList<>();
        for(YouthPolicyResponseDto policyDto : policies) {
            YouthPolicy entity = YouthPolicy.builder()
                    .rnum(policyDto.rnum())
                    .id(policyDto.id())
                    .title(policyDto.title())
                    .introduce(policyDto.introduce())
                    .regionCode(policyDto.regionCode())
                    .classificationCode(policyDto.classificationCode())
                    .ageInfo(policyDto.ageInfo())
                    .applicationDetails(policyDto.applicationDetails())
                    .residenceAndIncome(policyDto.residenceAndIncome())
                    .education(policyDto.education())
                    .specialization(policyDto.specialization())
                    .additionalNotes(policyDto.additionalNotes())
                    .participationRestrictions(policyDto.participationRestrictions())
                    .applicationProcess(policyDto.applicationProcess())
                    .screeningAndAnnouncement(policyDto.screeningAndAnnouncement())
                    .applicationSite(policyDto.applicationSite())
                    .submissionDocuments(policyDto.submissionDocuments())
                    .build();
            entities.add(entity);
        }
        return entities;
    }

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
        Page<YouthPolicy> youthPolicyPage;

        if (regionList != null && classificationList != null) {
            youthPolicyPage = youthPolicyRepository.findByRegionInAndClassificationIn(regionList, classificationList, pageable);
        } else if (regionList != null) {
            youthPolicyPage = youthPolicyRepository.findByRegionIn(regionList, pageable);
        } else if (classificationList != null) {
            youthPolicyPage = youthPolicyRepository.findByClassificationIn(classificationList, pageable);
        } else {
            youthPolicyPage = youthPolicyRepository.findAll(pageable);
        }

        List<PolicyListResponseDto> policyListResponseDtos = youthPolicyPage.getContent().stream()
                .map(policy -> createPolicyListResponseDto(user, policy))
                .collect(Collectors.toList());

        return policyListResponseDtos;
    }

    @Transactional
    public PolicyDetailResponseDto getPolicyDetail(Long userId, String policyId) {
        User user = getUserById(userId);
        YouthPolicy policy = getPolicyById(policyId);
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
        YouthPolicy policy = getPolicyById(policyId);

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
                YouthPolicy policy = youthPolicyRepository.findById(favoritePolicy.getPolicyId()).orElse(null);
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
                    .map(entry -> youthPolicyRepository.findById(entry.getKey())
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

    private boolean applyRegionAndClassificationFilter(YouthPolicy policy, List<EPolicyRegion> regionList, List<EPolicyClassification> classificationList) {
        // 정책이 지역 및 정책분야 필터 조건을 만족하는지 확인
        return (regionList.isEmpty() || regionList.contains(policy.getRegion()))
                && (classificationList.isEmpty() || classificationList.contains(policy.getClassification()));
    }

    private PolicyListResponseDto createPolicyListResponseDto(User user, YouthPolicy policy) {
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
        List<YouthPolicy> hotPolicyList = youthPolicyRepository.findHotPolicies();

        return hotPolicyList.stream()
                .filter(policy -> applyRegionAndClassificationFilter(policy, regionList, classificationList)) // 지역 & 정책분야 필터링 적용
                .limit(count) // 필요한 정책 수만큼 가져옴
                .map(policy -> createPolicyListResponseDto(user, policy))
                .collect(Collectors.toList());
    }

    public List<PolicyListResponseDto> getHotPolicyList(Long userId) {
        User user = getUserById(userId);
        List<YouthPolicy> hotPolicyList = youthPolicyRepository.findHotPolicies();

        return hotPolicyList.stream()
                .map(policy -> createPolicyListResponseDto(user, policy))
                .limit(6)
                .collect(Collectors.toList());
    }

}
