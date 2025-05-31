package com.example.withpeace.config;

import com.example.withpeace.type.EPolicyRegion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = {LegalDongCodeCache.class}) // LegalDongCodeCache만 로드
@TestPropertySource(locations = "classpath:application-local.yml")
class LegalDongCodeCacheTest {

    private static final Logger log = LoggerFactory.getLogger(LegalDongCodeCacheTest.class);

    @Autowired
    private LegalDongCodeCache legalDongCodeCache;

    @BeforeEach
    void setUp() throws IOException {
        legalDongCodeCache.loadRegionData();
    }

    @Test
    @DisplayName("testConvertRegionInfo_Nationwide")
    void testConvertRegionInfo_Nationwide() {
        // 전국에 해당하는 zipCd
        String zipCd = "11110,11140,11170,11200,11215,11230,11260,11290,11305,11320,11350,11380,11410,11440,11470,11500,11530,11545,11560,11590,11620,11650,11680,11710,11740,26110,26140,26170,26200,26230,26260,26290,26320,26350,26380,26410,26440,26470,26500,26530,26710,27110,27140,27170,27200,27230,27260,27290,27710,27720,28110,28140,28177,28185,28200,28237,28245,28260,28710,28720,29110,29140,29155,29170,29200,30110,30140,30170,30200,30230,31110,31140,31170,31200,31710,36110,41111,41113,41115,41117,41131,41133,41135,41150,41171,41173,41192,41194,41196,41210,41220,41250,41271,41273,41281,41285,41287,41290,41310,41360,41370,41390,41410,41430,41450,41461,41463,41465,41480,41500,41550,41570,41590,41610,41630,41650,41670,41800,41820,41830,43111,43112,43113,43114,43130,43150,43720,43730,43740,43745,43750,43760,43770,43800,44131,44133,44150,44180,44200,44210,44230,44250,44270,44710,44760,44770,44790,44800,44810,44825,46110,46130,46150,46170,46230,46710,46720,46730,46770,46780,46790,46800,46810,46820,46830,46840,46860,46870,46880,46890,46900,46910,47111,47113,47130,47150,47170,47190,47210,47230,47250,47280,47290,47730,47750,47760,47770,47820,47830,47840,47850,47900,47920,47930,47940,48121,48123,48125,48127,48129,48170,48220,48240,48250,48270,48310,48330,48720,48730,48740,48820,48840,48850,48860,48870,48880,48890,50110,50130,51110,51130,51150,51170,51190,51210,51230,51720,51730,51750,51760,51770,51780,51790,51800,51810,51820,51830,52111,52113,52130,52140,52180,52190,52210,52710,52720,52730,52740,52750,52770,52790,52800";

        Map<String, Object> result = legalDongCodeCache.convertRegionInfo(zipCd);

        log.info("region: {}", result.get("region"));
        log.info("residence: {}", result.get("residence"));
        assertEquals(Set.of(EPolicyRegion.전국), result.get("region"));
        assertEquals("전국", result.get("residence"));
    }

    @Test
    @DisplayName("testConvertRegionInfo_SingleRegion")
    void testConvertRegionInfo_SingleRegion() {
        // 특정 지역 하나 (전라남도 영암군)
        String zipCd = "46830";

        Map<String, Object> result = legalDongCodeCache.convertRegionInfo(zipCd);

        log.info("region: {}", result.get("region"));
        log.info("residence: {}", result.get("residence"));
        assertEquals(Set.of(EPolicyRegion.전남), result.get("region"));
        assertEquals("전라남도 영암군", result.get("residence"));
    }

    @Test
    @DisplayName("testConvertRegionInfo_MultipleRegions")
    void testConvertRegionInfo_MultipleRegions() {
        // 인천, 울산, 충북, 전북에 해당하는 zipCd
        String zipCd = "28110,28140,28177,28185,28200,28237,28245,28260,28710,28720,31110,31140,31170,31200,31710,43111,43112,43113,43114,43130,43150,43720,43730,43740,43745,43750,43760,43770,43800,52111,52113,52130,52140,52180,52190,52210,52710,52720,52730,52740,52750,52770,52790,52800";

        Map<String, Object> result = legalDongCodeCache.convertRegionInfo(zipCd);

        log.info("region: {}", result.get("region"));
        log.info("residence: {}", result.get("residence"));
        assertEquals(Set.of(EPolicyRegion.인천, EPolicyRegion.울산, EPolicyRegion.충북, EPolicyRegion.전북), result.get("region"));
        assertTrue(((String) result.get("residence")).contains("인천광역시 중구"));
        assertTrue(((String) result.get("residence")).contains("울산광역시 중구"));
        assertTrue(((String) result.get("residence")).contains("충청북도 청주시 상당구"));
        assertTrue(((String) result.get("residence")).contains("전북특별자치도 전주시 완산구"));
    }

    @Test
    @DisplayName("testConvertRegionInfo_InvalidZipCd")
    void testConvertRegionInfo_EmptyOrNull() {
        // 빈 문자열 또는 null 입력 시
        Map<String, Object> result1 = legalDongCodeCache.convertRegionInfo("");
        Map<String, Object> result2 = legalDongCodeCache.convertRegionInfo(null);

        // 결과 출력
        log.info("empty result: {}", result1);
        log.info("null result: {}", result2);

        assertEquals(Map.of("region", Set.of(), "residence", "-"), result1);
        assertEquals(Map.of("region", Set.of(), "residence", "-"), result2);
    }
}