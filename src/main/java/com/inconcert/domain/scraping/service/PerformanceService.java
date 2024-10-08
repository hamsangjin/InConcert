package com.inconcert.domain.scraping.service;

import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.category.repository.PostCategoryRepository;
import com.inconcert.domain.scraping.dto.ScrapedPostDTO;
import com.inconcert.domain.scraping.entity.Performance;
import com.inconcert.domain.scraping.repository.PerformanceRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.common.exception.ExceptionMessage;
import com.inconcert.common.exception.PostCategoryNotFoundException;
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
import java.util.Optional;
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
    private final ScrapingLoggingService scrapingLoggingService;

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
        }

        CompletableFuture.runAsync(() -> {
            try {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (int type = 1; type <= 4; type++) {
                    int finalType = type;
                    futures.add(CompletableFuture.runAsync(() ->
                            scrapingLoggingService.measureScrapingPerformance(() ->
                                    crawlPerformances(String.valueOf(finalType)))));
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

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
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

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
        User adminUser = userService.getUserByUsername("admin");

        Long type1 = 0L, type2 = 0L, type3 = 0L, type4 = 0L;
        for (WebElement element : elements) {
            String poster = element.findElement(By.cssSelector("a span:nth-child(1) img")).getAttribute("src");
            String title = element.findElement(By.cssSelector("a span:nth-child(2)")).getText();
            String date = element.findElement(By.cssSelector("a span:nth-child(3)")).getText();
            String place = element.findElement(By.cssSelector("a span:nth-child(4)")).getText();

            if (!title.isEmpty() && !date.isEmpty() && !place.isEmpty()) {
                try {
                    Performance performance = Performance.builder()
                            .title(title)
                            .imageUrl(poster)
                            .date(date)
                            .place(place)
                            .type(type)
                            .build();

                    // performance 저장
                    performanceRepository.save(performance);
                    log.info(performance.getTitle() + " Performance 저장");

                    if(!infoRepository.existsByTitle(title)) {
                        ScrapedPostDTO scrapedPostDTO = convertToCrawledDTO(performance, Long.parseLong(type));

                        Long typeByScore;
                        if(type.equals("1"))        typeByScore = type1++;
                        else if (type.equals("2"))  typeByScore = type3++;
                        else if (type.equals("3"))  typeByScore = type2++;
                        else                        typeByScore = type4++;
                        Post post = createPostFromCrawledDTO(scrapedPostDTO, adminUser, typeByScore);

                        // post 저장
                        infoRepository.save(post);
                        log.info("[" + post.getId() + "] " + post.getTitle() + " 게시글 저장");
                    }else{
                        log.error("[" + title + "] 중복된 게시글(제목)입니다");
                    }
                } catch (Exception e) {
                    log.error("Error saving performance or post: {}", e.getMessage());
                }
            } else {
                log.warn("Skipping element due to missing required information");
            }
        }
        driver.quit();
    }

    // 4시에 한번씩 새로 스크래핑
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void scheduleCrawling() {
        performanceRepository.deleteAll();
        startCrawlingAsync();
    }

    @Scheduled(cron = "0 5 4 * * ?")
    @Transactional
    public void scheduleRanking() {
        updateRank();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void scheduleDeleteEndPost() {
        List<Post> posts = infoRepository.findAll();

        for (Post post : posts) {
            if(post.getEndDate().isBefore(LocalDate.now()))     infoRepository.delete(post);
        }
    }

    // performance to CrawledDTO
    private ScrapedPostDTO convertToCrawledDTO(Performance performance, Long type) {
        if (type == 2L) type = 3L;
        else if (type == 3L) type = 2L;

        String postCategoryTitle = getPostCategoryTitle(type);
        String categoryTitle = "info";

        LocalDate endDate = parseEndDate(performance.getDate());

        return ScrapedPostDTO.builder()
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

    // CrawledPostDTO to Post
    private Post createPostFromCrawledDTO(ScrapedPostDTO scrapedPostDTO, User adminUser, Long typeByScore) {
        PostCategory postCategory = postCategoryRepository.findByTitleAndCategoryTitleWithCategory(
                scrapedPostDTO.getPostCategoryTitle(),
                scrapedPostDTO.getCategoryTitle()
        ).orElseThrow(() -> new PostCategoryNotFoundException(ExceptionMessage.POST_CATEGORY_NOT_FOUND.getMessage()));

        return Post.builder()
                .title(scrapedPostDTO.getTitle())
                .content(scrapedPostDTO.getContent())
                .endDate(scrapedPostDTO.getEndDate())
                .matchCount(scrapedPostDTO.getMatchCount())
                .thumbnailUrl(scrapedPostDTO.getThumbnailUrl())
                .postCategory(postCategory)
                .popularity(popularity(0, 0, 0, typeByScore))
                .user(adminUser)
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

    private void updateRank(){
        Long type1 = 0L, type2 = 0L, type3 = 0L, type4 = 0L;
        List<Performance> performances = performanceRepository.findAll();


        for (Performance performance : performances) {
            Optional<Post> optionalPost = infoRepository.findByTitle(performance.getTitle());

            if(optionalPost.isPresent()){
                Post post = optionalPost.get();

                switch (Integer.parseInt(performance.getType())){
                    case 1 -> post.updatePopularity(popularity(post.getViewCount(), post.getComments().size(), post.getLikes().size(), type1++));
                    case 2 -> post.updatePopularity(popularity(post.getViewCount(), post.getComments().size(), post.getLikes().size(), type2++));
                    case 3 -> post.updatePopularity(popularity(post.getViewCount(), post.getComments().size(), post.getLikes().size(), type3++));
                    case 4 -> post.updatePopularity(popularity(post.getViewCount(), post.getComments().size(), post.getLikes().size(), type4++));
                }
                infoRepository.save(post);
            }
        }
    }

    private double popularity(int viewCount, int commentCount, int likeCount, Long performanceId) {
        return (viewCount*0.01) + (commentCount*0.25) + (likeCount*0.25) - performanceId;
    }
}