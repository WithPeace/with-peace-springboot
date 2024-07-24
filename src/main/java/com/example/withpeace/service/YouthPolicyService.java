package com.example.withpeace.service;

import com.example.withpeace.domain.FavoritePolicy;
import com.example.withpeace.domain.User;
import com.example.withpeace.domain.YouthPolicy;
import com.example.withpeace.dto.response.*;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.FavoritePolicyRepository;
import com.example.withpeace.repository.UserRepository;
import com.example.withpeace.repository.YouthPolicyRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    private YouthPolicy getPolicyById(String policyId) {
        return youthPolicyRepository.findById(policyId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_YOUTH_POLICY));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
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
                .map(policy -> {
                    boolean isFavorite = favoritePolicyRepository.existsByUserAndPolicyId(user, policy.getId());
                    return PolicyListResponseDto.from(policy, isFavorite);
                })
                .collect(Collectors.toList());

        return policyListResponseDtos;
    }

    @Transactional
    public PolicyDetailResponseDto getPolicyDetail(String policyId) {
        YouthPolicy policy = getPolicyById(policyId);

        return PolicyDetailResponseDto.from(policy);
    }

    @Transactional
    public void registerFavoritePolicy(Long userId, String policyId) {
        User user = getUserById(userId);
        YouthPolicy policy = getPolicyById(policyId);
        FavoritePolicy favoritePolicy = favoritePolicyRepository.findByUserAndPolicyId(user, policyId);

        try{
            // 찜하기 되어있지 않은 경우 찜하기 처리 수행
            if(favoritePolicy == null) {
                favoritePolicyRepository.save(FavoritePolicy.builder()
                        .policyId(policy.getId())
                        .user(user)
                        .title(policy.getTitle())
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
            if(favoritePolicy != null)
                favoritePolicyRepository.delete(favoritePolicy);
        } catch (Exception e) {
            throw new CommonException(ErrorCode.FAVORITE_YOUTH_POLICY_ERROR);
        }
    }

}
