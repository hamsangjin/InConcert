package com.inconcert.global.service;

import com.inconcert.domain.crawling.entity.Performance;
import com.inconcert.domain.crawling.repository.PerformanceRepository;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.post.service.InfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void crawlIfNecessary() {
        Performance lastCrawl = performanceRepository.findTopByOrderByIdDesc();
        LocalDateTime now = LocalDateTime.now();

        // 크롤링이 되어있지 않은 상태
        if (lastCrawl == null) {
            performCrawling();
        }
        // 마지막으로 크롤링한 지 24시간이 지났을 때 다시 크롤링 (현재는 테스트 중이므로 1시간으로 설정함)
        else if(ChronoUnit.HOURS.between(lastCrawl.getUpdatedAt(), now) >= 24){
            performanceRepository.delete(lastCrawl);    // 이전 크롤링 지우기
            infoRepository.afterCrawling();
            performCrawling();
        }
    }

    @Transactional
    protected void performCrawling() {
        for (int type = 1; type <= 4; type++) {
            infoService.crawlAndSavePosts(String.valueOf(type));
        }
    }
}