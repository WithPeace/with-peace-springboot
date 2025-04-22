package com.example.withpeace.dto.response;

import com.example.withpeace.domain.Policy;
import com.example.withpeace.type.EPolicyClassification;
import lombok.Builder;

@Builder
public record PolicyDetailResponseDto(
        String id,
        String title,
        String introduce,
        EPolicyClassification classification,
        String applicationDetails,

        String applicationPeriodStatus,
        String operationPeriod,
        String ageInfo,
        String residence,
        String Income,
        String education,
        String specialization,
        String additionalNotes,
        String participationRestrictions,

        String applicationProcess,
        String screeningAndAnnouncement,
        String applicationSite,
        String submissionDocuments,

        String etc,
        String managingInstitution,
        String operatingOrganization,
        String referenceSite1,
        String referenceSite2,

        boolean isFavorite
) {
    public static PolicyDetailResponseDto from(Policy policy, boolean isFavorite) {
        return PolicyDetailResponseDto.builder()
                .id(policy.getId())
                .title(defaultIfNull(policy.getTitle()))
                .introduce(defaultIfNull(policy.getIntroduce()))
                .classification(policy.getClassification())
                .applicationDetails(defaultIfNull(policy.getApplicationDetails()))
                .applicationPeriodStatus(policy.getApplicationPeriodStatus())
                .operationPeriod(policy.getOperatingPeriod())
                .ageInfo(defaultIfNull(policy.getAge()))
                .residence(defaultIfNull(policy.getResidence()))
                .Income(defaultIfNull(policy.getIncome()))
                .education(defaultIfNull(policy.getEducation()))
                .specialization(defaultIfNull(policy.getSpecialization()))
                .additionalNotes(defaultIfNull(policy.getAdditionalNotes()))
                .participationRestrictions(defaultIfNull(policy.getParticipationRestrictions()))
                .applicationProcess(defaultIfNull(policy.getApplicationProcess()))
                .screeningAndAnnouncement(defaultIfNull(policy.getScreeningAndAnnouncement()))
                .applicationSite(defaultIfNull(policy.getApplicationSite()))
                .submissionDocuments(defaultIfNull(policy.getSubmissionDocuments()))
                .etc(defaultIfNull(policy.getEtc()))
                .managingInstitution(defaultIfNull(policy.getManagingInstitution()))
                .operatingOrganization(defaultIfNull(policy.getOperatingOrganization()))
                .referenceSite1(defaultIfNull(policy.getReferenceSite1()))
                .referenceSite2(defaultIfNull(policy.getReferenceSite2()))
                .isFavorite(isFavorite)
                .build();
    }

    private static String defaultIfNull(String value) {
        return value != null ? value : "-";
    }
}
