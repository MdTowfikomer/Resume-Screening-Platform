package com.resumeai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

// ═══════════════════════════════════════════════════════════════
//  ASYNC / THREAD POOL CONFIGURATION
// ═══════════════════════════════════════════════════════════════
@Configuration
class AsyncConfig {

    @Bean(name = "resumeProcessingExecutor")
    public Executor resumeProcessingExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("resume-proc-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Bean(name = "matchingExecutor")
    public Executor matchingExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("matching-");
        executor.initialize();
        return executor;
    }
}
