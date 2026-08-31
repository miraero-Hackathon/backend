package org.jejuro.miraero.domain.youthpolicy.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class YouthPolicyRegion {

    private final String regionCode;
    private final String regionName;
    private final boolean nationwide;
}
