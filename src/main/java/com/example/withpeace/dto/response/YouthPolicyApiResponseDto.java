package com.example.withpeace.dto.response;

import com.example.withpeace.config.LegalDongCodeCache;
import com.example.withpeace.domain.Policy;
import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyEducation;
import com.example.withpeace.type.EPolicyRegion;
import com.example.withpeace.type.EPolicySpecialization;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public record YouthPolicyApiResponseDto(
        @JsonProperty("plcyNo") String id, // 정책 id
        @JsonProperty("plcyNm") String title, // 정책명
        @JsonProperty("plcyExplnCn") String introduce, // 정책 소개
        @JsonProperty("bscPlanPlcyWayNo") String classificationCode, // 정책 분야 코드
        @JsonProperty("zipCd") String regionCodes, // 정책거주지역코드
        @JsonProperty("aplyPrdSeCd") String applicationPeriodCode, // 신청기간구분코드
        @JsonProperty("aplyYmd") String applicationPeriod, // 신청기간
        @JsonProperty("bizPrdBgngYmd") String businessStartDate, // 사업기간시작일자
        @JsonProperty("bizPrdEndYmd") String businessEndDate, // 사업기간종료일자
        @JsonProperty("bizPrdEtcCn") String businessEtc, // 사업기간기타내용

        @JsonProperty("sprtTrgtAgeLmtYn") String ageLimitYn, // 지원대상연령제한여부 (N: 제한O, Y: 제한X)
        @JsonProperty("sprtTrgtMinAge") String minAge, // 지원대상최소연령
        @JsonProperty("sprtTrgtMaxAge") String maxAge, // 지원대상최대연령
        @JsonProperty("plcySprtCn") String applicationDetails, // 신청 세부 사항
        @JsonProperty("earnMinAmt") String minIncome, // 소득최소금액
        @JsonProperty("earnMaxAmt") String maxIncome, // 소득최대금액
        @JsonProperty("earnEtcCn") String etcIncome, // 소득기타내용
        @JsonProperty("schoolCd") String educationCodes, // 학력 요건 코드
        @JsonProperty("plcyMajorCd") String specializationCodes, // 전공 요건 코드
        @JsonProperty("addAplyQlfcCndCn") String additionalNotes, // 추가 사항
        @JsonProperty("ptcpPrpTrgtCn") String participationRestrictions, // 참여 제한 사항
        @JsonProperty("plcyAplyMthdCn") String applicationProcess, // 신청 절차
        @JsonProperty("srngMthdCn") String screeningAndAnnouncement, // 심사 발표 내용
        @JsonProperty("aplyUrlAddr") String applicationSite, // 신청 사이트 주소
        @JsonProperty("sbmsnDcmntCn") String submissionDocuments, // 제출 서류 내용

        @JsonProperty("etcMttrCn") String etc, // 기타 유익 정보
        @JsonProperty("sprvsnInstCdNm") String managingInstitution, // 주관 기관
        @JsonProperty("operInstCdNm") String operatingOrganization, // 운영 기관
        @JsonProperty("refUrlAddr1") String referenceSite1, // 참고 사이트1
        @JsonProperty("refUrlAddr2") String referenceSite2 // 참고 사이트2
) {

        // DTO -> Entity 변환, 법정동 코드 변환
        public Policy toEntity(LegalDongCodeCache legalDongCodeCache, int sortOrder) {
                Map<String, Object> regionInfo = legalDongCodeCache.convertRegionInfo(regionCodes); // 정책거주지역코드 변환

                return Policy.builder()
                        .id(id)
                        .title(convertBlankToNull(title))
                        .introduce(convertBlankToNull(introduce))
                        .region((Set<EPolicyRegion>) regionInfo.get("region")) // 지역 변환
                        .classification(convertClassificationCode(classificationCode)) // 분야 코드 변환
                        .applicationPeriodStatus(convertApplicationPeriodStatus(applicationPeriodCode, applicationPeriod)) // 신청기간상태 변환
                        .operatingPeriod(convertOperatingPeriod(businessStartDate, businessEndDate, businessEtc)) // 운영기간 변환
                        .age(convertAgeInfo(ageLimitYn, minAge, maxAge)) // 연령 변환
                        .applicationDetails(convertBlankToNull(applicationDetails))
                        .residence((String) regionInfo.get("residence")) // 거주지 변환
                        .income(convertIncomeInfo(minIncome, maxIncome, etcIncome)) // 소득 정보 변환
                        .education(convertEducationCodes(educationCodes)) // 학력 요건 코드 변환
                        .specialization(convertSpecializationCodes(specializationCodes)) // 전공 요건 코드 변환
                        .additionalNotes(convertBlankToNull(additionalNotes))
                        .participationRestrictions(convertBlankToNull(participationRestrictions))
                        .applicationProcess(convertBlankToNull(applicationProcess))
                        .screeningAndAnnouncement(convertBlankToNull(screeningAndAnnouncement))
                        .applicationSite(convertBlankToNull(applicationSite))
                        .submissionDocuments(convertBlankToNull(submissionDocuments))
                        .etc(convertBlankToNull(etc))
                        .managingInstitution(convertBlankToNull(managingInstitution))
                        .operatingOrganization(convertBlankToNull(operatingOrganization))
                        .referenceSite1(convertBlankToNull(referenceSite1))
                        .referenceSite2(convertBlankToNull(referenceSite2))
                        .sortOrder(sortOrder) // 정렬 순서 반영
                        .build();
        }

        // 빈 문자열 또는 null을 null로 변환, 그 외 입력값은 그대로 반환
        private static String convertBlankToNull(String str) {
                return (str == null || str.isBlank()) ? null : str;
        }

        // 분야 코드 변환
        private static EPolicyClassification convertClassificationCode(String classificationCode) {
                return EPolicyClassification.fromCode(classificationCode);
        }

        // 신청기간상태 변환
        private String convertApplicationPeriodStatus(String applicationPeriodCode, String applicationPeriod) {
                // 0057001: 특정기간, 0057002: 상시, 0057003: 마감
                if ("0057002".equals(applicationPeriodCode)) return "상시";
                if ("0057003".equals(applicationPeriodCode)) return "마감";
                if ("0057001".equals(applicationPeriodCode) && applicationPeriod != null) {
                        String latestEndDate = findLatestEndDate(applicationPeriod);
                        if (latestEndDate != null) {
                                return calculateRemainingDays(latestEndDate);
                        }
                }

                log.warn("Invalid application period format: applicationPeriodCode={}, applicationPeriod={}",
                        applicationPeriodCode, applicationPeriod);
                return null; // 잘못된 코드일 경우 null 처리
        }

        // 여러 기간 중 가장 늦은 종료일을 찾는 메서드 (중복 제거)
        private String findLatestEndDate(String applicationPeriod) {
                try {
                        return Arrays.stream(applicationPeriod.split("[,\\n]|\\\\N")) // ',' 또는 '\n', '\N'을 구분자로 사용
                                .map(String::trim) // 앞뒤 공백 제거
                                .filter(period -> !period.isEmpty()) // 빈 문자열 제거
                                .map(period -> period.split(" ~ "))
                                .filter(dates -> dates.length == 2)
                                .map(dates -> dates[1]) // 종료일만 추출
                                .max(String::compareTo) // 가장 늦은 종료일 찾기
                                .orElse(null);
                } catch (Exception e) {
                        log.warn("Failed to parse application period: applicationPeriod={}, error={}", applicationPeriod, e.getMessage());
                        return null; // 변환 실패 시 null 처리
                }
        }


        // 신청기간 D-Day 변환
        private String calculateRemainingDays(String endDateStr) {
                try {
                        LocalDate today = LocalDate.now();
                        LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));

                        long daysRemaining = ChronoUnit.DAYS.between(today, endDate);

                        return daysRemaining > 0 ? "D-" + daysRemaining : "마감";
                } catch (Exception e) {
                        log.warn("Failed to parse endDate: endDateStr={}, error={}", endDateStr, e.getMessage());
                        return null; // 변환 실패 시 null 처리
                }
        }

        // 운영기간 변환
        private String convertOperatingPeriod(String businessStartDate, String businessEndDate, String businessEtc) {
                boolean hasStart = businessStartDate != null && !businessStartDate.isBlank();
                boolean hasEnd = businessEndDate != null && !businessEndDate.isBlank();
                boolean hasEtc = businessEtc != null && !businessEtc.isBlank();

                // 1. 시작일과 종료일이 모두 존재하는 경우
                if (hasStart && hasEnd) {
                        try {
                                LocalDate startDate = LocalDate.parse(businessStartDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                                LocalDate endDate = LocalDate.parse(businessEndDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

                                String period = startDate.format(formatter) + " ~ " + endDate.format(formatter);
                                return hasEtc ? period + "\n" + businessEtc : period; // 기타 내용이 있으면 추가
                        } catch (Exception e) {
                                log.warn("Failed to parse business period: businessStartDate={}, businessEndDate={}, error={}",
                                        businessStartDate, businessEndDate, e.getMessage());
                                return hasEtc ? businessEtc : null; // 날짜 파싱 실패 시 기타 내용 반환 또는 null 처리
                        }
                }

                // 2. 기타 내용만 존재하거나 모든 값이 없는 경우
                return hasEtc ? businessEtc : null; // 기타 내용 반환 또는 null 처리
        }

        // 연령 정보 변환
        private static String convertAgeInfo(String ageLimitYn, String minAge, String maxAge) {
                return "Y".equals(ageLimitYn) ? null : "만 " + minAge + "세 ~ 만 " + maxAge + "세";
        }

        // 소득 정보 변환
        private static String convertIncomeInfo(String minIncome, String maxIncome, String etcIncome) {
                boolean hasMin = minIncome != null && !minIncome.isBlank() && !"0".equals(minIncome);
                boolean hasMax = maxIncome != null && !maxIncome.isBlank() && !"0".equals(maxIncome);
                boolean hasEtc = etcIncome != null && !etcIncome.isBlank();

                // 1. 최소/최대 소득이 모두 존재하는 경우
                if (hasMin && hasMax) {
                        String range = "최소 " + minIncome + "원 ~ 최대 " + maxIncome + "원";
                        return hasEtc ? range + "\n" + etcIncome : range;
                }

                // 2. 기타 내용만 존재하는 경우
                if (hasEtc) {
                        return etcIncome;
                }

                // 3. 모든 값이 없는 경우
                return null;
        }

        // 학습 요건 코드 변환 및 콤마로 구분하여 변환
        private static String convertEducationCodes(String educationCodes) {
                if (educationCodes == null || educationCodes.isBlank()) return null;
                return Arrays.stream(educationCodes.split(","))
                        .map(EPolicyEducation::fromCode)
                        .collect(Collectors.joining(", "));
        }

        private static String convertSpecializationCodes(String specializationCodes) {
                if (specializationCodes == null || specializationCodes.isBlank()) return null;
                return Arrays.stream(specializationCodes.split(","))
                        .map(code -> EPolicySpecialization.fromCode(code).name())
                        .collect(Collectors.joining(", "));
        }
}
