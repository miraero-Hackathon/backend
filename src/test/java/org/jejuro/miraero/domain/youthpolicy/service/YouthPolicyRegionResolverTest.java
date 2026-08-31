package org.jejuro.miraero.domain.youthpolicy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicyRegion;
import org.junit.jupiter.api.Test;

class YouthPolicyRegionResolverTest {

    @Test
    void resolve_convertsMultipleSigunguCodesToSidoRegions() {
        List<YouthPolicyRegion> regions = YouthPolicyRegionResolver.resolve("11000,11680,41110");

        assertEquals(2, regions.size());
        assertEquals("11", regions.get(0).getRegionCode());
        assertEquals("서울특별시", regions.get(0).getRegionName());
        assertEquals("41", regions.get(1).getRegionCode());
    }

    @Test
    void resolve_marksExplicitNationwideCodeAsNationwide() {
        List<YouthPolicyRegion> regions = YouthPolicyRegionResolver.resolve("00000");

        assertEquals(1, regions.size());
        assertEquals("00", regions.get(0).getRegionCode());
        assertTrue(regions.get(0).isNationwide());
    }

    @Test
    void resolve_normalizesSpecialSelfGoverningProvinceCodes() {
        List<YouthPolicyRegion> regions = YouthPolicyRegionResolver.resolve("51110,52110");

        assertEquals("42", regions.get(0).getRegionCode());
        assertEquals("45", regions.get(1).getRegionCode());
    }

    @Test
    void resolve_doesNotTreatMissingZipCodeAsNationwide() {
        assertTrue(YouthPolicyRegionResolver.resolve(null).isEmpty());
        assertTrue(YouthPolicyRegionResolver.resolve(" ").isEmpty());
    }

    @Test
    void resolveRegionCode_acceptsCurrentRegionNameAndCode() {
        assertEquals("11", YouthPolicyRegionResolver.resolveRegionCode("서울특별시"));
        assertEquals("11", YouthPolicyRegionResolver.resolveRegionCode("11"));
    }
}
