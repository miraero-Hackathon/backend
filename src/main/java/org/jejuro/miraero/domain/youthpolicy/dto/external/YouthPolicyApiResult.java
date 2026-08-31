package org.jejuro.miraero.domain.youthpolicy.dto.external;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class YouthPolicyApiResult {

    private YouthPolicyApiPaging pagging;
    private List<YouthPolicyApiItem> youthPolicyList;
}
