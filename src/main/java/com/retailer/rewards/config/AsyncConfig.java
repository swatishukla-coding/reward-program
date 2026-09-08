package com.retailer.rewards.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Dedicated thread pool for the (simulated) asynchronous transaction lookups,
 * kept separate from the servlet request-handling threads.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "rewardsTaskExecutor")
    public TaskExecutor rewardsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("rewards-async-");
        executor.initialize();
        return executor;
    }
}
