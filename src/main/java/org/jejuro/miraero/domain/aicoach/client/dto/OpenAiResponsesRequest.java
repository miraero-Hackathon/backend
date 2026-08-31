package org.jejuro.miraero.domain.aicoach.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiResponsesRequest {

    private String model;
    private String input;
    private Reasoning reasoning;
    private Boolean stream;

    public OpenAiResponsesRequest(String model, String input, String reasoningEffort) {
        this(model, input, reasoningEffort, null);
    }

    public OpenAiResponsesRequest(
            String model,
            String input,
            String reasoningEffort,
            Boolean stream
    ) {
        this.model = model;
        this.input = input;
        this.reasoning = new Reasoning(reasoningEffort);
        this.stream = stream;
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Reasoning {

        private final String effort;

        public Reasoning(String effort) {
            this.effort = effort;
        }
    }
}
