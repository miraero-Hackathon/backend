package org.jejuro.miraero.domain.youthpolicy.dto.external;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class YouthPolicyApiResponse {

    private Integer resultCode;
    private String resultMessage;
    private YouthPolicyApiResult result;
}
