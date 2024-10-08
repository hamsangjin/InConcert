package com.inconcert.domain.scraping.service;

import com.inconcert.common.annotation.LogExecutionTime;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ScrapingLoggingService {
    @Async
    @LogExecutionTime
    public void measureScrapingPerformance(Runnable task) {
        task.run();
    }
}