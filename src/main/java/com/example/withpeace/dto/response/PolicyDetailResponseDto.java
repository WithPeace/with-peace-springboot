package com.example.withpeace.dto.response;

import com.example.withpeace.domain.YouthPolicy;
import com.example.withpeace.type.EPolicyClassification;
import lombok.Builder;

@Builder
public record PolicyDetailResponseDto(
        String id,
        String title,
        String introduce,
        EPolicyClassification classification,
        String applicationDetails,

        String ageInfo,
        String residenceAndIncome,
        String education,
        String specialization,
        String additionalNotes,
        String participationRestrictions,

        String applicationProcess,
        String screeningAndAnnouncement,
        String applicationSite,
        String submissionDocuments,

        String additionalUsefulInformation, // etc
        String supervisingAuthority, // managingInstitution
        String operatingOrganization,
        String businessRelatedReferenceSite1, // businessReferenceSite1
        String businessRelatedReferenceSite2, // businessReferenceSite2

        boolean isFavorite
) {
    public static PolicyDetailResponseDto from(YouthPolicy policy, boolean isFavorite) {
        return PolicyDetailResponseDto.builder()
                .id(policy.getId())
                .title(policy.getTitle())
                .introduce(policy.getIntroduce())
                .classification(policy.getClassification())
                .applicationDetails(policy.getApplicationDetails())
                .ageInfo(policy.getAgeInfo())
                .residenceAndIncome(policy.getResidenceAndIncome())
                .education(policy.getEducation())
                .specialization(policy.getSpecialization())
                .additionalNotes(policy.getAdditionalNotes())
                .participationRestrictions(policy.getParticipationRestrictions())
                .applicationProcess(policy.getApplicationProcess())
                .screeningAndAnnouncement(policy.getScreeningAndAnnouncement())
                .applicationSite(policy.getApplicationSite())
                .submissionDocuments(policy.getSubmissionDocuments())
                .additionalUsefulInformation(policy.getEtc())
                .supervisingAuthority(policy.getManagingInstitution())
                .operatingOrganization(policy.getOperatingOrganization())
                .businessRelatedReferenceSite1(policy.getBusinessReferenceSite1())
                .businessRelatedReferenceSite2(policy.getBusinessReferenceSite2())
                .isFavorite(isFavorite)
                .build();
    }
}
