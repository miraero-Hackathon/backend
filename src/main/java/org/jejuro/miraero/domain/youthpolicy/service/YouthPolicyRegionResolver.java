package org.jejuro.miraero.domain.youthpolicy.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyRegion;

final class YouthPolicyRegionResolver {

    private static final String NATIONWIDE_REGION_CODE = "00";
    private static final String NATIONWIDE_REGION_NAME = "전국";
    private static final String NATIONWIDE_ZIP_CODE = "00000";
    private static final String NATIONWIDE_TEXT = "전국";
    private static final Map<String, String> REGION_NAMES = createRegionNames();
    private static final Map<String, String> REGION_CODE_ALIASES = Map.of(
            "51", "42",
            "52", "45"
    );
    private static final Map<String, String> REGION_CODES_BY_NAME = createRegionCodesByName();

    private YouthPolicyRegionResolver() {
    }

    static List<YouthPolicyRegion> resolve(String zipCd) {
        if (zipCd == null || zipCd.isBlank()) {
            return List.of();
        }

        Set<String> regionCodes = new LinkedHashSet<>();
        for (String value : zipCd.split(",")) {
            String normalizedValue = value.trim();
            if (isNationwideValue(normalizedValue)) {
                return List.of(nationwideRegion());
            }
            if (normalizedValue.matches("\\d{5}")) {
                String regionCode = normalizeRegionCode(normalizedValue.substring(0, 2));
                if (REGION_NAMES.containsKey(regionCode)) {
                    regionCodes.add(regionCode);
                }
            }
        }

        if (regionCodes.containsAll(REGION_NAMES.keySet())) {
            return List.of(nationwideRegion());
        }

        List<YouthPolicyRegion> regions = new ArrayList<>();
        for (String regionCode : regionCodes) {
            regions.add(new YouthPolicyRegion(regionCode, REGION_NAMES.get(regionCode), false));
        }
        return regions;
    }

    static String resolveRegionCode(String region) {
        if (region == null || region.isBlank()) {
            return null;
        }

        String normalizedRegion = region.trim();
        if (REGION_NAMES.containsKey(normalizedRegion)) {
            return normalizedRegion;
        }
        return REGION_CODES_BY_NAME.get(normalizedRegion);
    }

    private static String normalizeRegionCode(String regionCode) {
        return REGION_CODE_ALIASES.getOrDefault(regionCode, regionCode);
    }

    private static boolean isNationwideValue(String value) {
        return NATIONWIDE_ZIP_CODE.equals(value) || NATIONWIDE_TEXT.equals(value);
    }

    private static YouthPolicyRegion nationwideRegion() {
        return new YouthPolicyRegion(NATIONWIDE_REGION_CODE, NATIONWIDE_REGION_NAME, true);
    }

    private static Map<String, String> createRegionNames() {
        Map<String, String> regionNames = new LinkedHashMap<>();
        regionNames.put("11", "서울특별시");
        regionNames.put("26", "부산광역시");
        regionNames.put("27", "대구광역시");
        regionNames.put("28", "인천광역시");
        regionNames.put("29", "광주광역시");
        regionNames.put("30", "대전광역시");
        regionNames.put("31", "울산광역시");
        regionNames.put("36", "세종특별자치시");
        regionNames.put("41", "경기도");
        regionNames.put("42", "강원특별자치도");
        regionNames.put("43", "충청북도");
        regionNames.put("44", "충청남도");
        regionNames.put("45", "전북특별자치도");
        regionNames.put("46", "전라남도");
        regionNames.put("47", "경상북도");
        regionNames.put("48", "경상남도");
        regionNames.put("50", "제주특별자치도");
        return Map.copyOf(regionNames);
    }

    private static Map<String, String> createRegionCodesByName() {
        Map<String, String> regionCodesByName = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : REGION_NAMES.entrySet()) {
            regionCodesByName.putIfAbsent(entry.getValue(), entry.getKey());
        }
        regionCodesByName.put("강원도", "42");
        regionCodesByName.put("전라북도", "45");
        return Map.copyOf(regionCodesByName);
    }
}
