package com.inconcert.domain.crawling.service;

import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.category.repository.PostCategoryRepository;
import com.inconcert.domain.crawling.dto.CrawledPostDTO;
import com.inconcert.domain.crawling.entity.Performance;
import com.inconcert.domain.crawling.repository.PerformanceRepository;
import com.inconcert.domain.post.dto.PostDTO;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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

    private volatile boolean isCrawling = false;
    private final Object crawlingLock = new Object();

    public boolean isCrawling() {
        return isCrawling;
    }

    @Async
    public void startCrawlingAsync() {
        // type 별로 별도의 스레드에서 스크래핑
        synchronized (crawlingLock) {
            if (isCrawling) {
                log.info("Crawling is already in progress. Skipping this request.");
                return;
            }
            isCrawling = true;
            // sse 에 시작 메시지 전송
            crawlingSseEmitters.sendStatusUpdate("started", "Crawling process has started");
        }

        CompletableFuture.runAsync(() -> {
            try {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (int type = 1; type <= 4; type++) {
                    int finalType = type;
                    futures.add(CompletableFuture.runAsync(() -> crawlPerformances(String.valueOf(finalType))));
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                // sse 에 완료 메시지 전송
                crawlingSseEmitters.sendStatusUpdate("completed", "All performances have been successfully crawled.");
                log.info("Crawling process completed and notification sent");
            } finally {
                synchronized (crawlingLock) {
                    isCrawling = false;
                }
            }
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
        List<PostDTO> batchPostDTOs = new ArrayList<>();
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

                        // performance 저장
                        performanceRepository.save(performance);
                        log.info("Saved performance: " + performance.getTitle());

                        CrawledPostDTO crawledPostDTO = convertToCrawledDTO(performance, Long.parseLong(type));
                        Post post = createPostFromCrawledDTO(crawledPostDTO);

                        // post 저장
                        Post savedPost = infoRepository.save(post);
                        log.info("Saved post: {}", post.getTitle());
                        log.info("Saved post id: {}", post.getId());

                        // 실시간 업데이트 전송
                        PostDTO postDTO = convertToPostDTO(crawledPostDTO, savedPost);
                        postDTO.setId(savedPost.getId());   // id가 꼬이게 하지 않기 위함

                        // 배치 처리를 위해 리스트에 추가
                        batchPostDTOs.add(postDTO);
                        processedCount++;

                        // 배치 크기에 도달하면 배치 업데이트 수행
                        if (batchPostDTOs.size() >= BATCH_SIZE) {
                            crawlingSseEmitters.sendBatchUpdate(batchPostDTOs);
                            log.info("Sent batch update with {} posts", batchPostDTOs.size());
                            batchPostDTOs.clear();
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

        if (!batchPostDTOs.isEmpty()) {
            crawlingSseEmitters.sendBatchUpdate(batchPostDTOs);
            log.info("Sent batch update with {} posts", batchPostDTOs.size());
            batchPostDTOs.clear();
        }
        driver.quit();
    }

    // CrawledPostDTO to Post
    @Transactional(readOnly = true)
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

    // performance to CrawledDTO
    private CrawledPostDTO convertToCrawledDTO(Performance performance, Long type) {
        if (type == 2L) type = 3L;
        else if (type == 3L) type = 2L;

        String postCategoryTitle = getPostCategoryTitle(type);
        String categoryTitle = "info";

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
                .build();
    }

    // to PostDTO
    private PostDTO convertToPostDTO(CrawledPostDTO crawledPostDTO, Post post) {
        return PostDTO.builder()
                .id(post.getId())
                .title(crawledPostDTO.getTitle())
                .content(crawledPostDTO.getContent())
                .endDate(crawledPostDTO.getEndDate())
                .matchCount(crawledPostDTO.getMatchCount())
                .thumbnailUrl(crawledPostDTO.getThumbnailUrl())
                .categoryTitle(crawledPostDTO.getCategoryTitle())
                .postCategoryTitle(crawledPostDTO.getPostCategoryTitle())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private String getPostCategoryTitle(Long type) {
        return switch (type.intValue()) {
            case 1 -> "musical";
            case 2 -> "concert";
            case 3 -> "theater";
            case 4 -> "etc";
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
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

    // 자정에 한번씩 새로 스크래핑
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleCrawling() {
        startCrawlingAsync();
    }
}