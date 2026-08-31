package org.jejuro.miraero.domain.youthpolicy.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class YouthPolicyApiPaging {

    private Integer totCount;
    private Integer pageNum;
    private Integer pageSize;
}
