package com.example.withpeace.type;

import java.util.HashMap;
import java.util.Map;

public enum EPolicyRegion {
    중앙부처("003001"),
    서울("003002001"),
    부산("003002002"),
    대구("003002003"),
    인천("003002004"),
    광주("003002005"),
    대전("003002006"),
    울산("003002007"),
    경기("003002008"),
    강원("003002009"),
    충북("003002010"),
    충남("003002011"),
    전북("003002012"),
    전남("003002013"),
    경북("003002014"),
    경남("003002015"),
    제주("003002016"),
    세종("003002017"),
    기타("");

    private final String code;

    EPolicyRegion(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    // 코드 값을 통해 EPolicyRegion을 찾기 위해 HashMap 사용
    private static final Map<String, EPolicyRegion> codeToRegionMap = new HashMap<>();

    static {
        for (EPolicyRegion region : EPolicyRegion.values()) {
            codeToRegionMap.put(region.getCode(), region);
        }
    }

    public static EPolicyRegion fromCode(String code) {
        if (code.substring(0, 6).equals(중앙부처.getCode())){
            return 중앙부처;
        } else if (code.length() >= 9) {
            String substringCode = code.substring(0, 9);
            return codeToRegionMap.getOrDefault(substringCode, 기타);
        } else {
            return 기타;
        }
    }
}
