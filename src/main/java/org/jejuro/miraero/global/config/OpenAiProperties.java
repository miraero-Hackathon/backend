package org.jejuro.miraero.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OpenAiProperties {

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final int timeoutMs;
    private final int maxOutputTokens;
    private final String reasoningEffort;
    private final boolean moderationEnabled;

    public OpenAiProperties(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model}") String model,
            @Value("${openai.base-url}") String baseUrl,
            @Value("${openai.timeout-ms}") int timeoutMs,
            @Value("${openai.max-output-tokens:700}") int maxOutputTokens,
            @Value("${openai.reasoning-effort:low}") String reasoningEffort,
            @Value("${openai.moderation.enabled:false}") boolean moderationEnabled
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
        this.maxOutputTokens = maxOutputTokens;
        this.reasoningEffort = reasoningEffort;
        this.moderationEnabled = moderationEnabled;
    }
}
