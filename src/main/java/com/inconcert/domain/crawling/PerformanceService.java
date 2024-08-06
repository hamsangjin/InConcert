package com.inconcert.domain.crawling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceService {
    private final PerformanceRepository performanceRepository;

    @Transactional
    public void crawlAndSavePerformances(String categoryId) throws IOException {
        String url = null;
        if(Integer.parseInt(categoryId) >= 3) {
            url = "http://m.playdb.co.kr/Play/List?maincategory=00000" + categoryId + "&playtype=3";
        }
        else url = "http://m.playdb.co.kr/Play/List?maincategory=00000" + categoryId;

        Document doc = Jsoup.connect(url).get();
        Elements performances = doc.select("#list li");

        for (Element performanceElement : performances) {
            Performance performance = new Performance();
            performance.setImageUrl(performanceElement.select("li a span:nth-child(1) img").attr("src"));
            performance.setTitle(performanceElement.select("li a span:nth-child(2)").text());
            performance.setDate(performanceElement.select("li a span:nth-child(3)").text());
            performance.setPlace(performanceElement.select("li a span:nth-child(4)").text());

            performanceRepository.save(performance);
            log.info("저장된 공연: {}", performance.getTitle());
        }
    }
}
