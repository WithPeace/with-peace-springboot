package com.example.withpeace.type;

import java.util.HashMap;
import java.util.Map;

public enum EPolicySpecialization {
    인문계열("0011001"),
    사회계열("0011002"),
    상경계열("0011003"),
    이학계열("0011004"),
    공학계열("0011005"),
    예체능계열("0011006"),
    농산업계열("0011007"),
    기타("0011008"),
    제한없음("0011009");

    private final String code;

    EPolicySpecialization(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    // 코드 값을 통해 EPolicySpecialization 찾기 위해 HashMap 사용
    private static final Map<String, EPolicySpecialization> codeToSpecializationMap = new HashMap<>();

    static {
        for (EPolicySpecialization specialization : EPolicySpecialization.values()) {
            codeToSpecializationMap.put(specialization.getCode(), specialization);
        }
    }

    public static EPolicySpecialization fromCode(String code) {
        if (code == null || !codeToSpecializationMap.containsKey(code)) {
            return EPolicySpecialization.기타; // 기본값: 기타
        }
        return codeToSpecializationMap.get(code);
    }
}
