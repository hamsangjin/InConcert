package com.inconcert.domain.crawling.service;

import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.category.repository.PostCategoryRepository;
import com.inconcert.domain.crawling.dto.CrawledPostDTO;
import com.inconcert.domain.crawling.entity.Performance;
import com.inconcert.domain.crawling.repository.PerformanceRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.PostCategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final InfoRepository infoRepository;
    private final UserService userService;
    private final CrawlingSseEmitters crawlingSseEmitters;
    private static final int BATCH_SIZE = 10;

    @Async
    public CompletableFuture<Void> startCrawlingAsync() {
        return CompletableFuture.runAsync(() -> {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int type = 1; type <= 4; type++) {
                int finalType = type;
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        crawlPerformances(String.valueOf(finalType));
                    } catch (Exception e) {
                        log.error("Error while crawling performances for type {}: {}", finalType, e.getMessage());
                    }
                }));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 크롤링 완료 후 클라이언트에 알림 전송
            crawlingSseEmitters.sendUpdate(CrawledPostDTO.builder()
                    .title("Crawling completed")
                    .content("")
                    .endDate(null)
                    .matchCount(0)
                    .thumbnailUrl("")
                    .categoryTitle("info")
                    .postCategoryTitle("info")
                    .createdAt(LocalDateTime.now())
                    .viewCount(0)
                    .build());
            log.info("Crawling process completed and notification sent");
        });
    }

    public WebDriver getChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        return driver;
    }

    @Transactional
    public void crawlPerformances(String type) {
        String url = (Integer.parseInt(type) >= 3)
                ? "http://m.playdb.co.kr/Play/List?maincategory=00000" + type + "&playtype=3"
                : "http://m.playdb.co.kr/Play/List?maincategory=00000" + type;

        WebDriver driver = getChromeDriver();
        driver.get(url);

        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        long lastHeight = (long) jsExecutor.executeScript("return document.body.scrollHeight");

        while (true) {
            jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread was interrupted: ", e);
            }

            long newHeight = (long) jsExecutor.executeScript("return document.body.scrollHeight");
            if (newHeight == lastHeight) {
                break;
            }
            lastHeight = newHeight;
        }

        List<WebElement> elements = driver.findElements(By.cssSelector("#list li"));
        List<CrawledPostDTO> batchCrawledPostDTOs = new ArrayList<>();
        int processedCount = 0;

        for (WebElement element : elements) {
            String poster = element.findElement(By.cssSelector("a span:nth-child(1) img")).getAttribute("src");
            String title = element.findElement(By.cssSelector("a span:nth-child(2)")).getText();
            String date = element.findElement(By.cssSelector("a span:nth-child(3)")).getText();
            String place = element.findElement(By.cssSelector("a span:nth-child(4)")).getText();

            if (!title.isEmpty() && !date.isEmpty() && !place.isEmpty()) {
                try {
                    if(!performanceRepository.existsByTitle(title)) {
                        Performance performance = Performance.builder()
                                .title(title)
                                .imageUrl(poster)
                                .date(date)
                                .place(place)
                                .type(type)
                                .build();


                        performanceRepository.save(performance);
                        log.info("Saved performance: " + performance.getTitle());


                        CrawledPostDTO crawledPostDTO = convertToCrawledDTO(performance, Long.parseLong(type));
                        Post post = createPostFromCrawledDTO(crawledPostDTO);
                        infoRepository.save(post);
                        log.info("Saved post: {}", post.getTitle());

                        // 실시간 업데이트 전송
                        crawlingSseEmitters.sendUpdate(crawledPostDTO);

                        // 배치 처리를 위해 리스트에 추가
                        batchCrawledPostDTOs.add(crawledPostDTO);
                        processedCount++;

                        // 배치 크기에 도달하면 배치 업데이트 수행
                        if (batchCrawledPostDTOs.size() >= BATCH_SIZE) {
                            crawlingSseEmitters.sendBatchUpdate(batchCrawledPostDTOs);
                            log.info("Sent batch update with {} posts", batchCrawledPostDTOs.size());
                            batchCrawledPostDTOs.clear();
                        }
                    }else{
                        log.error("Duplicated performance");
                    }
                } catch (Exception e) {
                    log.error("Error saving performance or post: {}", e.getMessage());
                }
            } else {
                log.warn("Skipping element due to missing required information");
            }
        }

        if (!batchCrawledPostDTOs.isEmpty()) {
            crawlingSseEmitters.sendBatchUpdate(batchCrawledPostDTOs);
            log.info("Sent final batch update with {} posts", batchCrawledPostDTOs.size());
        }
        driver.quit();
    }

    @Transactional
    public Post createPostFromCrawledDTO(CrawledPostDTO crawledPostDTO) {
        User adminUser = userService.getUserByUsername("admin");

        PostCategory postCategory = postCategoryRepository.findByTitleAndCategoryTitleWithCategory(
                crawledPostDTO.getPostCategoryTitle(),
                crawledPostDTO.getCategoryTitle()
        ).orElseThrow(() -> new PostCategoryNotFoundException(ExceptionMessage.POST_CATEGORY_NOT_FOUND.getMessage()));

        return Post.builder()
                .title(crawledPostDTO.getTitle())
                .content(crawledPostDTO.getContent())
                .endDate(crawledPostDTO.getEndDate())
                .matchCount(crawledPostDTO.getMatchCount())
                .thumbnailUrl(crawledPostDTO.getThumbnailUrl())
                .postCategory(postCategory)
                .user(adminUser)
                .build();
    }

    private CrawledPostDTO convertToCrawledDTO(Performance performance, Long type) {

        if (type == 2L) type = 3L;
        else if (type == 3L) type = 2L;

        String postCategoryTitle = getPostCategoryTitle(type);
        String categoryTitle = getCategoryTitle(type);


        LocalDate endDate = parseEndDate(performance.getDate());

        return CrawledPostDTO.builder()
                .id(performance.getId())
                .title(performance.getTitle())
                .content("<img src=" + performance.getImageUrl() + "><br><span>장소: </span><span>" + performance.getPlace() + "</span><br><span>날짜: </span><span>"+ performance.getDate()+"</span>")
                .endDate(endDate)
                .matchCount(0)
                .thumbnailUrl(performance.getImageUrl())
                .categoryTitle(categoryTitle)
                .postCategoryTitle(postCategoryTitle)
                .createdAt(LocalDateTime.now())
                .viewCount(0)
                .commentCount(0)
                .build();
    }

    private String getPostCategoryTitle(Long type) {
        switch (type.intValue()) {
            case 1: return "musical";
            case 2: return "concert";
            case 3: return "theater";
            case 4: return "etc";
            default: throw new IllegalArgumentException("Invalid type: " + type);
        }
    }

    private String getCategoryTitle(Long type) {
        return "info";  // Assuming all types belong to the 'info' category
    }

    private LocalDate parseEndDate(String dateString) {
        try {
            String[] dateParts = dateString.split("~");
            if (dateParts.length > 1) {
                String endDatePart = dateParts[1].trim();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                return LocalDate.parse(endDatePart, formatter);
            }
        } catch (DateTimeParseException e) {
            log.error("Date parsing error: ", e);
        }
        return LocalDate.now();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleCrawling() {
        startCrawlingAsync();
    }


}