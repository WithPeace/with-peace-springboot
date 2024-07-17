package com.example.withpeace.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YouthPolicyResponseDto(
        @JacksonXmlProperty(localName = "rnum")
        String rnum,

        @JacksonXmlProperty(localName = "bizId")
        String id,

        @JacksonXmlProperty(localName = "polyBizSjnm")
        String title,

        @JacksonXmlProperty(localName = "polyItcnCn")
        String introduce,

        @JacksonXmlProperty(localName = "polyRlmCd")
        String classificationCode,

        @JacksonXmlProperty(localName = "polyBizSecd")
        String regionCode,


        @JacksonXmlProperty(localName = "ageInfo")
        String ageInfo,

        @JacksonXmlProperty(localName = "sporCn")
        String applicationDetails,

        @JacksonXmlProperty(localName = "prcpCn")
        String residenceAndIncome,

        @JacksonXmlProperty(localName = "accrRqisCn")
        String education,

        @JacksonXmlProperty(localName = "splzRlmRqisCn")
        String specialization,

        @JacksonXmlProperty(localName = "aditRscn")
        String additionalNotes,

        @JacksonXmlProperty(localName = "prcpLmttTrgtCn")
        String participationRestrictions,

        @JacksonXmlProperty(localName = "rqutProcCn")
        String applicationProcess,

        @JacksonXmlProperty(localName = "jdgnPresCn")
        String screeningAndAnnouncement,

        @JacksonXmlProperty(localName = "rqutUrla")
        String applicationSite,

        @JacksonXmlProperty(localName = "pstnPaprCn")
        String submissionDocuments,


        @JacksonXmlProperty(localName = "etct")
        String etc,

        @JacksonXmlProperty(localName = "mngtMson")
        String managingInstitution,

        @JacksonXmlProperty(localName = "cnsgNmor")
        String operatingOrganization,

        @JacksonXmlProperty(localName = "rfcSiteUrla1")
        String businessReferenceSite1,

        @JacksonXmlProperty(localName = "rfcSiteUrla2")
        String businessReferenceSite2
) {
}
