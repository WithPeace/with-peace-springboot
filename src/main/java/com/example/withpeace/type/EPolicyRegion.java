package com.example.withpeace.type;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum EPolicyRegion {
    전국("전국", "NATIONWIDE"),
    서울("서울특별시", "SEOUL"),
    부산("부산광역시", "BUSAN"),
    대구("대구광역시", "DAEGU"),
    인천("인천광역시", "INCHEON"),
    광주("광주광역시", "GWANGJU"),
    대전("대전광역시", "DAEJEON"),
    울산("울산광역시", "ULSAN"),
    경기("경기도", "GYEONGGI"),
    강원("강원특별자치도", "GANGWON"),
    충북("충청북도", "CHUNGBUK"),
    충남("충청남도", "CHUNGNAM"),
    전북("전북특별자치도", "JEONBUK"),
    전남("전라남도", "JEONNAM"),
    경북("경상북도", "GYEONGBUK"),
    경남("경상남도", "GYEONGNAM"),
    제주("제주특별자치도", "JEJU"),
    세종("세종특별자치시", "SEJONG"),
    기타("기타", "ETC");

    private final String fullName;
    private final String englishName;


    private static final Map<String, EPolicyRegion> FULL_NAME_TO_ENUM_MAP =
            Arrays.stream(EPolicyRegion.values())
                            .collect(Collectors.toMap(EPolicyRegion::getFullName, e -> e));

    private static final Map<String, EPolicyRegion> ENGLISH_NAME_TO_ENUM_MAP =
            Arrays.stream(EPolicyRegion.values())
                    .collect(Collectors.toMap(EPolicyRegion::getEnglishName, e -> e));

    EPolicyRegion(String fullName, String englishName) {
        this.fullName = fullName;
        this.englishName = englishName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEnglishName() {
        return englishName;
    }

    /**
     * 주어진 광역시/도 이름을 기반으로 EPolicyRegion Enum 반환
     * @param name 법정동 코드에서 제공하는 광역시/도 이름
     * @return EPolicyRegion Enum 값 (없으면 기타 반환)
     */
    public static EPolicyRegion fromFullName(String name) {
        return FULL_NAME_TO_ENUM_MAP.getOrDefault(name, 기타);
    }

    /**
     * 영어명을 기반으로 EPolicyRegion Enum 반환
     * @param name 요청된 영어 지역명
     * @return EPolicyRegion Enum 값 (없으면 기타 반환)
     */
    public static EPolicyRegion fromEnglishName(String name) {
        return ENGLISH_NAME_TO_ENUM_MAP.getOrDefault(name, 기타);
    }
}
