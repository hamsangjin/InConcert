package com.inconcert.domain.crawling.service;

import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.category.repository.PostCategoryRepository;
import com.inconcert.domain.crawling.entity.Performance;
import com.inconcert.domain.crawling.repository.PerformanceRepository;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.PostCategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.JavascriptExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceService {
    @Value("${chrome.driver}")
    private String webDriverPath;   // chromedriver 위치

    private final PerformanceRepository performanceRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final InfoRepository infoRepository;
    private final UserService userService;

    public WebDriver getChromeDriver() {
        if (ObjectUtils.isEmpty(System.getProperty("webdriver.chrome.driver"))) {
            System.setProperty("webdriver.chrome.driver", webDriverPath);
        }

        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // 브라우저를 띄우지 않음
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        return driver;
    }

    public void crawlPerformances(String type) {
        String url = null;

        if (Integer.parseInt(type) >= 3) {
            url = "http://m.playdb.co.kr/Play/List?maincategory=00000" + type + "&playtype=3";
        } else {
            url = "http://m.playdb.co.kr/Play/List?maincategory=00000" + type;
        }

        WebDriver driver = getChromeDriver();
        driver.get(url);

        // 페이지 끝까지 스크롤
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        long lastHeight = (long) jsExecutor.executeScript("return document.body.scrollHeight");

        while (true) {
            jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            try {
                Thread.sleep(2000); // 잠시 대기하여 새로운 콘텐츠가 로드되도록 함
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long newHeight = (long) jsExecutor.executeScript("return document.body.scrollHeight");
            if (newHeight == lastHeight) {
                break;
            }
            lastHeight = newHeight;
        }

        List<WebElement> elements = driver.findElements(By.cssSelector("#list li"));

        for (WebElement element : elements) {
            try {
                String poster = "";
                String title = "";
                String date = "";
                String place = "";

                try {
                    poster = element.findElement(By.cssSelector("a span:nth-child(1) img")).getAttribute("src");
                } catch (Exception e) {
                    log.warn("Failed to find poster for an element");
                }

                try {
                    title = element.findElement(By.cssSelector("a span:nth-child(2)")).getText();
                } catch (Exception e) {
                    log.warn("Failed to find title for an element");
                }

                try {
                    date = element.findElement(By.cssSelector("a span:nth-child(3)")).getText();
                } catch (Exception e) {
                    log.warn("Failed to find date for an element");
                }

                try {
                    place = element.findElement(By.cssSelector("a span:nth-child(4)")).getText();
                } catch (Exception e) {
                    log.warn("Failed to find place for an element");
                }

                if (!title.isEmpty() && !date.isEmpty() && !place.isEmpty()) {
                    log.info("eijfaoe??!?!?!?! {} {} {} {} {}", title, poster, date, place, type);
                    Performance performance = Performance.builder()
                            .title(title)
                            .imageUrl(poster)
                            .date(date)
                            .place(place)
                            .type(type)
                            .build();
                    performanceRepository.save(performance);
                    System.out.println("Saved performance: " + performance.getTitle());

                    saveAsPost(performance, Long.parseLong(type));
                } else {
                    log.warn("Skipping element due to missing required information");
                }
            } catch (Exception e) {
                log.error("Failed to parse element: " + e.getMessage());
            }
        }

        driver.quit();
    }

    @Transactional
    protected void saveAsPost(Performance performance, Long type) {
        try {
            PostCategory postCategory = postCategoryRepository.findById(type)
                    .orElseThrow(() -> new PostCategoryNotFoundException("PostCategory not found"));

            User user = userService.findByUsername("admin");    // 작성자는 항상 관리자

            LocalDate endDate;
            try {
                String[] dateParts = performance.getDate().split("~");
                if (dateParts.length > 1) {
                    String endDatePart = dateParts[1].trim();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                    endDate = LocalDate.parse(endDatePart, formatter);
                } else {
                    endDate = LocalDate.now();
                }
            } catch (DateTimeParseException e) {
                log.error("Date parsing error: ", e);
                endDate = LocalDate.now();
            }

            PostDto postDto = PostDto.builder()
                    .title(performance.getTitle())
                    .content("<img src=" + performance.getImageUrl() + "><br><span>장소: </span><span>" + performance.getPlace() + "</span><br><span>날짜: </span><span>"+ performance.getDate()+"</span>")
                    .endDate(endDate) // assuming the date is in the format "start_date ~ end_date"
                    .matchCount(0)
                    .postCategory(postCategory)
                    .comments(new HashSet<>())
                    .likeCount(0)
                    .commentCount(0)
                    .thumbnailUrl(performance.getImageUrl())
                    .user(user)
                    .build();

            Post post = PostDto.toEntity(postDto, postCategory);

            infoRepository.save(post);
            log.info("Saved post: {}", post.getTitle());
        } catch (Exception e) {
            log.error("Error saving post: ", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }
}