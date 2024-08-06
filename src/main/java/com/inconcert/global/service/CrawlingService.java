package com.inconcert.global.service;

import com.inconcert.domain.crawling.entity.Performance;
import com.inconcert.domain.crawling.repository.PerformanceRepository;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.post.service.InfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class CrawlingService {
    private final InfoService infoService;
    private final InfoRepository infoRepository;
    private final PerformanceRepository performanceRepository;

//    // 서버가 항상 실행될 때 사용하므로 주석 처리함 (현재는 로컬에서 작업 중)
//    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
//    public void scheduledCrawling() {
//        for (int type = 1; type <= 4; type++) {
//            infoService.crawlAndSavePosts(String.valueOf(type));
//        }
//    }

    public void crawlIfNecessary() {
        Performance lastCrawl = performanceRepository.findTopByOrderByIdDesc();
        if(lastCrawl != null) performanceRepository.delete(lastCrawl);    // 이전 크롤릴 지우기
        LocalDateTime now = LocalDateTime.now();

        // 마지막으로 크롤링한 지 24시간이 지났을 때 다시 크롤링
        if (lastCrawl == null || ChronoUnit.HOURS.between(lastCrawl.getUpdatedAt(), now) >= 24) {
            infoRepository.afterCrawling();
            performCrawling();
        }
    }

    private void performCrawling() {
        for (int type = 1; type <= 4; type++) {
            infoService.crawlAndSavePosts(String.valueOf(type));
        }
    }
}
