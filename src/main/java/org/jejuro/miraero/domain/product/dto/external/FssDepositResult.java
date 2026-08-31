package org.jejuro.miraero.domain.product.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FssDepositResult {

    @JsonProperty("prdt_div")
    private String productDivision;

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("max_page_no")
    private Integer maxPageNo;

    @JsonProperty("now_page_no")
    private Integer nowPageNo;

    @JsonProperty("err_cd")
    private String errorCode;

    @JsonProperty("err_msg")
    private String errorMessage;

    @JsonSetter(value = "baseList", nulls = Nulls.AS_EMPTY)
    private List<FssDepositProduct> baseList = new ArrayList<>();

    @JsonSetter(value = "optionList", nulls = Nulls.AS_EMPTY)
    private List<FssDepositOption> optionList = new ArrayList<>();
}
