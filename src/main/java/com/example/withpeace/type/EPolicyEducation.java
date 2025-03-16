package com.example.withpeace.type;

import java.util.HashMap;
import java.util.Map;

public enum EPolicyEducation {
    고졸_미만("0049001", "고졸 미만"),
    고교_재학("0049002", "고교 재학"),
    고졸_예정("0049003", "고졸 예정"),
    고교_졸업("0049004", "고교 졸업"),
    대학_재학("0049005", "대학 재학"),
    대졸_예정("0049006", "대졸 예정"),
    대학_졸업("0049007", "대학 졸업"),
    석_박사("0049008", "석·박사"),
    기타("0049009", "기타"),
    제한없음("0049010", "제한없음");

    private final String code;
    private final String name;

    EPolicyEducation(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() { return code; }
    public String getName() { return name; }

    // 코드 값을 통해 EPolicyEducation 찾기 위해 HashMap 사용
    private static final Map<String, EPolicyEducation> codeToEducationMap = new HashMap<>();

    static {
        for (EPolicyEducation education : EPolicyEducation.values()) {
            codeToEducationMap.put(education.getCode(), education);
        }
    }

    public static String fromCode(String code) {
        if (code == null || !codeToEducationMap.containsKey(code)) {
            return EPolicyEducation.기타.getName(); // 기본값: 기타
        }
        return codeToEducationMap.get(code).getName();
    }
}
