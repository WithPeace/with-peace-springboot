package com.example.withpeace.domain;

import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "policies")
public class Policy {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id; // 정책 id

    @Column(name = "title", length = 512)
    private String title; // 정책명

    @Column(name = "introduce", columnDefinition = "TEXT")
    private String introduce; // 정책 소개

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "policy_regions",
            joinColumns = @JoinColumn(name = "policy_id", referencedColumnName = "id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "region")
    private Set<EPolicyRegion> region; // 지역 (광역시/도)

    @Enumerated(EnumType.STRING)
    @Column(name = "classification")
    private EPolicyClassification classification; // 정책 분야 코드

    @Column(name = "application_period_status", length = 10)
    private String applicationPeriodStatus; // 신청기간상태 ("상시", "D-??", "마감")

    @Column(name = "operating_period", length = 512)
    private String operatingPeriod; // 운영기간


    @Column(name = "age", length = 512)
    private String age; // 연령 정보

    @Column(name = "application_details", columnDefinition = "TEXT")
    private String applicationDetails; // 신청 세부 사항

    @Column(name = "residence", columnDefinition = "TEXT")
    private String residence; // 거주지 (시/군/구)

    @Column(name = "income", length = 512)
    private String income; // 소득 조건

    @Column(name = "education", length = 512)
    private String education; // 학력 요건

    @Column(name = "specialization")
    private String specialization; // 전공 요건

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes; // 추가 사항

    @Column(name = "participation_restrictions", columnDefinition = "TEXT")
    private String participationRestrictions; // 참여 제한 사항

    @Column(name = "application_process", columnDefinition = "TEXT")
    private String applicationProcess; // 신청 절차

    @Column(name = "screening_and_announcement", columnDefinition = "TEXT")
    private String screeningAndAnnouncement; // 심사 발표 내용

    @Column(name = "application_site", columnDefinition = "TEXT")
    private String applicationSite; // 신청 사이트 주소

    @Column(name = "submission_documents", columnDefinition = "TEXT")
    private String submissionDocuments; // 제출 서류 내용


    @Column(name = "etc", columnDefinition = "TEXT")
    private String etc; // 기타 유익 정보

    @Column(name = "managing_institution", length = 512)
    private String managingInstitution; // 주관 기관

    @Column(name = "operating_organization", length = 512)
    private String operatingOrganization; // 운영 기관

    @Column(name = "reference_site1", length = 512)
    private String referenceSite1; // 사업관련 참고 사이트1

    @Column(name = "reference_site2", length = 512)
    private String referenceSite2; // 사업관련 참고 사이트2

    // 정책 순서 설정
    @Setter
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;; // Open API 정책 순서 반영

    @Builder
    public Policy(String id, String title, String introduce, Set<EPolicyRegion> region,
                  EPolicyClassification classification, String applicationPeriodStatus, String operatingPeriod, String age, String applicationDetails,
                  String residence, String income, String education, String specialization,
                  String additionalNotes, String participationRestrictions, String applicationProcess,
                  String screeningAndAnnouncement, String applicationSite, String submissionDocuments,
                  String etc, String managingInstitution, String operatingOrganization,
                  String referenceSite1, String referenceSite2, int sortOrder) {
        this.id = id;
        this.title = title;
        this.introduce = introduce;
        this.region = region;
        this.classification = classification;
        this.applicationPeriodStatus = applicationPeriodStatus;
        this.operatingPeriod = operatingPeriod;
        this.age = age;
        this.applicationDetails = applicationDetails;
        this.residence = residence;
        this.income = income;
        this.education = education;
        this.specialization = specialization;
        this.additionalNotes = additionalNotes;
        this.participationRestrictions = participationRestrictions;
        this.applicationProcess = applicationProcess;
        this.screeningAndAnnouncement = screeningAndAnnouncement;
        this.applicationSite = applicationSite;
        this.submissionDocuments = submissionDocuments;
        this.etc = etc;
        this.managingInstitution = managingInstitution;
        this.operatingOrganization = operatingOrganization;
        this.referenceSite1 = referenceSite1;
        this.referenceSite2 = referenceSite2;
        this.sortOrder = sortOrder;
    }

    // JPA 변경 감지(Dirty Checking) 활용하여 기존 데이터 업데이트
    public boolean updateAllFieldsFrom(Policy updatedPolicy) {
        boolean isUpdated = false;

        if (!Objects.equals(this.title, updatedPolicy.getTitle())) {
            this.title = updatedPolicy.getTitle();
            isUpdated = true;
        }
        if (!Objects.equals(this.introduce, updatedPolicy.getIntroduce())) {
            this.introduce = updatedPolicy.getIntroduce();
            isUpdated = true;
        }
        if (!Objects.equals(this.region, updatedPolicy.getRegion())) {
            this.region = updatedPolicy.getRegion();
            isUpdated = true;
        }
        if (this.classification != updatedPolicy.getClassification()) {
            this.classification = updatedPolicy.getClassification();
            isUpdated = true;
        }
        if (!Objects.equals(this.applicationPeriodStatus, updatedPolicy.getApplicationPeriodStatus())) {
            this.applicationPeriodStatus = updatedPolicy.getApplicationPeriodStatus();
            isUpdated = true;
        }
        if (!Objects.equals(this.operatingPeriod, updatedPolicy.getOperatingPeriod())) {
            this.operatingPeriod = updatedPolicy.getOperatingPeriod();
            isUpdated = true;
        }
        if (!Objects.equals(this.age, updatedPolicy.getAge())) {
            this.age = updatedPolicy.getAge();
            isUpdated = true;
        }
        if (!Objects.equals(this.applicationDetails, updatedPolicy.getApplicationDetails())) {
            this.applicationDetails = updatedPolicy.getApplicationDetails();
            isUpdated = true;
        }
        if (!Objects.equals(this.residence, updatedPolicy.getResidence())) {
            this.residence = updatedPolicy.getResidence();
            isUpdated = true;
        }
        if (!Objects.equals(this.income, updatedPolicy.getIncome())) {
            this.income = updatedPolicy.getIncome();
            isUpdated = true;
        }
        if (!Objects.equals(this.education, updatedPolicy.getEducation())) {
            this.education = updatedPolicy.getEducation();
            isUpdated = true;
        }
        if (!Objects.equals(this.specialization, updatedPolicy.getSpecialization())) {
            this.specialization = updatedPolicy.getSpecialization();
            isUpdated = true;
        }
        if (!Objects.equals(this.additionalNotes, updatedPolicy.getAdditionalNotes())) {
            this.additionalNotes = updatedPolicy.getAdditionalNotes();
            isUpdated = true;
        }
        if (!Objects.equals(this.participationRestrictions, updatedPolicy.getParticipationRestrictions())) {
            this.participationRestrictions = updatedPolicy.getParticipationRestrictions();
            isUpdated = true;
        }
        if (!Objects.equals(this.applicationProcess, updatedPolicy.getApplicationProcess())) {
            this.applicationProcess = updatedPolicy.getApplicationProcess();
            isUpdated = true;
        }
        if (!Objects.equals(this.screeningAndAnnouncement, updatedPolicy.getScreeningAndAnnouncement())) {
            this.screeningAndAnnouncement = updatedPolicy.getScreeningAndAnnouncement();
            isUpdated = true;
        }
        if (!Objects.equals(this.applicationSite, updatedPolicy.getApplicationSite())) {
            this.applicationSite = updatedPolicy.getApplicationSite();
            isUpdated = true;
        }
        if (!Objects.equals(this.submissionDocuments, updatedPolicy.getSubmissionDocuments())) {
            this.submissionDocuments = updatedPolicy.getSubmissionDocuments();
            isUpdated = true;
        }
        if (!Objects.equals(this.etc, updatedPolicy.getEtc())) {
            this.etc = updatedPolicy.getEtc();
            isUpdated = true;
        }
        if (!Objects.equals(this.managingInstitution, updatedPolicy.getManagingInstitution())) {
            this.managingInstitution = updatedPolicy.getManagingInstitution();
            isUpdated = true;
        }
        if (!Objects.equals(this.operatingOrganization, updatedPolicy.getOperatingOrganization())) {
            this.operatingOrganization = updatedPolicy.getOperatingOrganization();
            isUpdated = true;
        }
        if (!Objects.equals(this.referenceSite1, updatedPolicy.getReferenceSite1())) {
            this.referenceSite1 = updatedPolicy.getReferenceSite1();
            isUpdated = true;
        }
        if (!Objects.equals(this.referenceSite2, updatedPolicy.getReferenceSite2())) {
            this.referenceSite2 = updatedPolicy.getReferenceSite2();
            isUpdated = true;
        }
        if (this.sortOrder != updatedPolicy.getSortOrder()) {
            this.sortOrder = updatedPolicy.getSortOrder();
            isUpdated = true;
        }

        return isUpdated; // 변경이 발생했는지 여부 반환
    }
}
