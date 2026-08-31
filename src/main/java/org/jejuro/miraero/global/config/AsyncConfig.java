package org.jejuro.miraero.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "milestoneReportExecutor")
    public Executor milestoneReportExecutor() {

        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix(
                "milestone-report-"
        );

        executor.initialize();

        return executor;
    }

    @Bean(name = "aiCoachStreamExecutor")
    public Executor aiCoachStreamExecutor() {
        return createExecutor(2, 10, 30, "ai-coach-stream-");
    }

    @Bean(name = "aiCoachSummaryExecutor")
    public Executor aiCoachSummaryExecutor() {
        return createExecutor(1, 1, 50, "ai-coach-summary-");
    }

    private Executor createExecutor(
            int corePoolSize,
            int maxPoolSize,
            int queueCapacity,
            String threadNamePrefix
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
}
