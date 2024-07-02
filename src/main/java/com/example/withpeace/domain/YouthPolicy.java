package com.example.withpeace.domain;

import com.example.withpeace.type.EPolicyClassification;
import com.example.withpeace.type.EPolicyRegion;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "youth_policies")
public class YouthPolicy {
    @Id
    @Column(name = "rnum", nullable = false, unique = true)
    private Long rnum;

    @Column(name = "id", nullable = false)
    private String id; // 정책 id

    @Column(name = "title")
    private String title; // 정책명

    @Column(name = "introduce")
    private String introduce; // 정책 소개

    @Enumerated(EnumType.STRING)
    @Column(name = "region")
    private EPolicyRegion region; // 지역 코드

    @Enumerated(EnumType.STRING)
    @Column(name = "classification")
    private EPolicyClassification classification; // 정책 분야 코드


    @Column(name = "age_info")
    private String ageInfo; // 연령 정보

    @Column(name = "application_details")
    private String applicationDetails; // 신청 세부 사항

    @Column(name = "residence_and_income")
    private String residenceAndIncome; // 거주지 및 소득 조건

    @Column(name = "education")
    private String education; // 학력 요건

    @Column(name = "specialization")
    private String specialization; // 전공 요건

    @Column(name = "additional_notes")
    private String additionalNotes; // 추가 사항

    @Column(name = "participation_restrictions")
    private String participationRestrictions; // 참여 제한 사항

    @Column(name = "application_process")
    private String applicationProcess; // 신청 절차

    @Column(name = "screening_and_announcement")
    private String screeningAndAnnouncement; // 심사 발표 내용

    @Column(name = "application_site")
    private String applicationSite; // 신청 사이트 주소

    @Column(name = "submission_documents")
    private String submissionDocuments; // 제출 서류 내용


    @Column(name = "etc")
    private String etc; // 기타 유익 정보

    @Column(name = "managing_institution")
    private String managingInstitution; // 주관 기관

    @Column(name = "operating_organization")
    private String operatingOrganization; // 운영 기관

    @Column(name = "business_reference_site1")
    private String businessReferenceSite1; // 사업관련 참고 사이트1

    @Column(name = "business_reference_site2")
    private String businessReferenceSite2; // 사업관련 참고 사이트2

    @Builder
    public YouthPolicy(String rnum, String id, String title, String introduce, String regionCode, String classificationCode,
                       String ageInfo, String applicationDetails, String residenceAndIncome, String education,
                       String specialization, String additionalNotes, String participationRestrictions,
                       String applicationProcess, String screeningAndAnnouncement, String applicationSite,
                       String submissionDocuments, String etc, String managingInstitution, String operatingOrganization,
                       String businessReferenceSite1, String businessReferenceSite2) {
        this.rnum = Long.parseLong(rnum);
        this.id = id;
        this.title = title;
        this.introduce = introduce == null || introduce.equals("null") ? "-" : introduce;
        this.region = EPolicyRegion.fromCode(regionCode);
        this.classification = EPolicyClassification.fromCode(classificationCode);
        this.ageInfo = ageInfo == null || ageInfo.equals("null") ? "-" : ageInfo;
        this.applicationDetails = applicationDetails == null || applicationDetails.equals("null") ? "-" : applicationDetails;
        this.residenceAndIncome = residenceAndIncome == null || residenceAndIncome.equals("null") ? "-" : residenceAndIncome;
        this.education = education == null || education.equals("null") ? "-" : education;
        this.specialization = specialization == null || specialization.equals("null") ? "-" : specialization;
        this.additionalNotes = additionalNotes == null || additionalNotes.equals("null") ? "-" : additionalNotes;
        this.participationRestrictions = participationRestrictions == null || participationRestrictions.equals("null") ? "-" : participationRestrictions;
        this.applicationProcess = applicationProcess == null || applicationProcess.equals("null") ? "-" : applicationProcess;
        this.screeningAndAnnouncement = screeningAndAnnouncement == null || screeningAndAnnouncement.equals("null") ? "-" : screeningAndAnnouncement;
        this.applicationSite = applicationSite == null || applicationSite.equals("null") ? "-" : applicationSite;
        this.submissionDocuments = submissionDocuments == null || submissionDocuments.equals("null") ? "-" : submissionDocuments;
        this.etc = etc == null || etc.equals("null") ? "-" : etc;
        this.managingInstitution = managingInstitution == null || managingInstitution.equals("null") ? "-" : managingInstitution;
        this.operatingOrganization = operatingOrganization == null || operatingOrganization.equals("null") ? "-" : operatingOrganization;
        this.businessReferenceSite1 = businessReferenceSite1 == null || businessReferenceSite1.equals("null") ? "-" : businessReferenceSite1;
        this.businessReferenceSite2 = businessReferenceSite2 == null || businessReferenceSite2.equals("null") ? "-" : businessReferenceSite2;
    }
}
