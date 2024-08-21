package com.inconcert.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private String keyword;
    private String message;
    private boolean isRead;
    private String type;
    private LocalDateTime createdAt;
    private String categoryTitle;
    private String postCategoryTitle;
    private Long postId;
}