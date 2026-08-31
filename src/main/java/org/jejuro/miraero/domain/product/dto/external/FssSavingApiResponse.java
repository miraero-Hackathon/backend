package org.jejuro.miraero.domain.product.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FssSavingApiResponse {

    @JsonProperty("result")
    private FssSavingResult result;
}
