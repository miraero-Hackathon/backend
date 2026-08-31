package org.jejuro.miraero.domain.youthpolicy.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class YouthPolicyApiItem {

    private String plcyNo;
    private String plcyNm;
    private String plcyKywdNm;
    private String plcyExplnCn;
    private String plcySprtCn;
    private String sprvsnInstCd;
    private String sprvsnInstCdNm;
    private String aplyYmd;
    private String plcyAplyMthdCn;
    private String aplyUrlAddr;
    private String refUrlAddr1;
    private String sprtTrgtMinAge;
    private String sprtTrgtMaxAge;
    private String earnCndSeCd;
    private String earnMinAmt;
    private String earnMaxAmt;
    private String earnEtcCn;
    private String addAplyQlfcCndCn;
    private String ptcpPrpTrgtCn;
    private String lastMdfcnDt;
    private String zipCd;
}
