package com.example.withpeace.config;

import com.example.withpeace.exception.CommonException;
import com.example.withpeace.exception.ErrorCode;
import com.example.withpeace.type.EPolicyRegion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
public class LegalDongCodeCache {

    @Value("${file.regionCodeDataPath}")
    private String filePath; // 법정동 코드 데이터 파일 경로

    private static final Map<String, String> regionCodeToNameMap = new ConcurrentHashMap<>(); // 시/군/구 코드 맵핑

    /**
     * 법정동 코드 데이터를 로드하여 시/군/구 정보 저장
     */
    @PostConstruct
    protected void loadRegionData() {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                processRegionData(br);
            }
        } catch (IOException e) {
            log.error("Failed to load region data: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.YOUTH_POLICY_REGION_LOAD_ERROR);
        }
    }

    /**
     * 실제 데이터 처리 로직
     */
    private void processRegionData(BufferedReader br) throws IOException {
        Set<String> processedCodes = new HashSet<>();
        String line;
        boolean firstLine = true;
        int totalLines = 0, skippedLines = 0, duplicateCount = 0, validCount = 0;

        while ((line = br.readLine()) != null) {
            totalLines++;

            if (firstLine) {
                firstLine = false;
                continue;
            }

            String[] parts = line.split("\\t"); // 탭으로 구분
            if (parts.length < 3) {
                log.warn("Invalid data format: {}", line);
                skippedLines++;
                continue;
            }

            String code = parts[0].trim(); // 법정동 코드 (10자리)
            String name = parts[1].trim(); // 행정구역명
            String status = parts[2].trim(); // 폐지 여부

            if (!"존재".equals(status)) {
                skippedLines++;
                continue; // 폐지된 지역 제외
            }

            if (code.length() < 10) {
                log.warn("Invalid region code format: {}", code);
                skippedLines++;
                continue;
            }

            String districtCode = code.substring(0, 5); // 시/군/구 코드 (앞 5자리만)

            // 중복 여부 확인
            if (!processedCodes.add(districtCode)) {
                duplicateCount++;
                log.debug("중복된 코드 발견: {}", districtCode);
                continue;
            }

            // 시/군/구 저장
            regionCodeToNameMap.put(districtCode, name);
            validCount++;
        }

        log.info("Processed {} lines: {} valid, {} duplicates, {} skipped",
                totalLines, validCount, duplicateCount, skippedLines);
    }

    /**
     * 주어진 법정동 코드 리스트를 지역 정보로 변환함
     * @param regionCodes 콤마(,)로 구분된 법정동 코드 문자열
     * @return 변환된 지역 정보 (region: 광역시/도, residence: 상세 지역명)
     */
    public Map<String, Object> convertRegionInfo(String regionCodes) {
        Map<String, Object> result = new HashMap<>();

        // 입력값이 비어 있으면 기본값 반환
        if (regionCodes == null || regionCodes.isBlank()) {
            result.put("region", new HashSet<>());
            result.put("residence", "-");
            return result;
        }

        String[] codes = regionCodes.split(",");
        Set<EPolicyRegion> uniqueRegions = new HashSet<>();
        List<String> residenceList = new ArrayList<>();

        for (String code : codes) {
            // 10자리 코드에서 앞 5자리만 사용
            String trimmedCode = code.length() >= 5 ? code.substring(0, 5) : code;

            // 시/군/구 정보 가져오기
            String regionName = regionCodeToNameMap.get(trimmedCode);

            if (regionName != null) {
                residenceList.add(regionName);

                // 광역시/도만 추출
                String broadRegion = extractBroadRegion(regionName);
                uniqueRegions.add(EPolicyRegion.fromFullName(broadRegion));
            }
        }

        // 모든 광역시/도가 포함되었을 경우 "전국"으로 설정
        if (uniqueRegions.size() == 17) {
            result.put("region", new HashSet<>(List.of(EPolicyRegion.전국)));
            result.put("residence", "전국");
        } else {
            result.put("region", uniqueRegions);
            result.put("residence", residenceList.isEmpty() ? "-" : String.join(", ", residenceList));
        }

        return result;
    }

    /**
     * 광역시/도를 추출 (예: "경기도 안성시" -> "경기도").
     *
     * @param regionName 시/군/구 전체 이름
     * @return 해당되는 광역시/도 이름
     */
    private String extractBroadRegion(String regionName) {
        for (EPolicyRegion region : EPolicyRegion.values()) {
            if (regionName.startsWith(region.getFullName())) {
                return region.getFullName();
            }
        }
        return "기타";
    }
}
