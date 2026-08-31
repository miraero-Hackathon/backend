package org.jejuro.miraero.domain.aicoach.client;

public interface OpenAiModerationClient {

    boolean isFlagged(String content);
}
