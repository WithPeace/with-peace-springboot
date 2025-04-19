package com.example.withpeace.type;

import java.util.HashMap;
import java.util.Map;

public enum EPolicyClassification {
    JOB("001"),
    RESIDENT("002"),
    EDUCATION("003"),
    WELFARE_AND_CULTURE("004"),
    PARTICIPATION_AND_RIGHT("005"),
    ETC("");

    private final String code;

    EPolicyClassification(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    // 코드 값을 통해 EPolicyClassification을 찾기 위해 HashMap 사용
    private static final Map<String, EPolicyClassification> codeToClassificationMap = new HashMap<>();

    static {
        for (EPolicyClassification classification : EPolicyClassification.values()) {
            codeToClassificationMap.put(classification.getCode(), classification);
        }
    }

    public static EPolicyClassification fromCode(String code) {
        if (code == null || !codeToClassificationMap.containsKey(code)) {
            return EPolicyClassification.ETC;
        }
        return codeToClassificationMap.get(code);
    }
}
