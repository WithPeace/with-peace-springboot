package com.example.withpeace.service;

import com.example.withpeace.domain.YouthPolicy;
import com.example.withpeace.dto.response.YouthPolicyListResponseDto;
import com.example.withpeace.dto.response.YouthPolicyResponseDto;
import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.repository.YouthPolicyRepository;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouthPolicyService {

    @Value("${youth-policy.api-key}")
    private String apiKey;

    private int saveCount = 0;

    private final YouthPolicyRepository youthPolicyRepository;

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

}
