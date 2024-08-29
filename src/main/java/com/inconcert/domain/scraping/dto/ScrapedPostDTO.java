package com.inconcert.domain.scraping.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapedPostDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDate endDate;
    private int matchCount;
    private String thumbnailUrl;
    private String categoryTitle;
    private String postCategoryTitle;
}
