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

    @Builder
    public YouthPolicy(String rnum, String id, String title, String introduce, String regionCode, String classificationCode,
                       String ageInfo, String applicationDetails, String residenceAndIncome, String education,
                       String specialization, String additionalNotes, String participationRestrictions,
                       String applicationProcess, String screeningAndAnnouncement, String applicationSite,
                       String submissionDocuments) {
        this.rnum = Long.parseLong(rnum);
        this.id = id;
        this.title = title;
        this.introduce = introduce.equals("null") ? "-" : introduce;
        this.region = EPolicyRegion.fromCode(regionCode);
        this.classification = EPolicyClassification.fromCode(classificationCode);
        this.ageInfo = ageInfo.equals("null") ? "-" : ageInfo;
        this.applicationDetails = applicationDetails.equals("null") ? "-" : applicationDetails;
        this.residenceAndIncome = residenceAndIncome.equals("null") ? "-" : residenceAndIncome;
        this.education = education.equals("null") ? "-" : education;
        this.specialization = specialization.equals("null") ? "-" : specialization;
        this.additionalNotes = additionalNotes.equals("null") ? "-" : additionalNotes;
        this.participationRestrictions = participationRestrictions.equals("null") ? "-" : participationRestrictions;
        this.applicationProcess = applicationProcess.equals("null") ? "-" : applicationProcess;
        this.screeningAndAnnouncement = screeningAndAnnouncement.equals("null") ? "-" : screeningAndAnnouncement;
        this.applicationSite = applicationSite.equals("null") ? "-" : applicationSite;
        this.submissionDocuments = submissionDocuments.equals("null") ? "-" : submissionDocuments;
    }
}
