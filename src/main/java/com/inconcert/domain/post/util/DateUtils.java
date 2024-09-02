package com.inconcert.domain.post.util;

import java.time.LocalDateTime;

public class DateUtils {
    public static LocalDateTime getStartDate(String period) {
        return switch (period) {
            case "all" -> LocalDateTime.now().minusYears(100);
            case "1day" -> LocalDateTime.now().minusDays(1);
            case "1week" -> LocalDateTime.now().minusWeeks(1);
            case "1month" -> LocalDateTime.now().minusMonths(1);
            case "6months" -> LocalDateTime.now().minusMonths(6);
            case "1year" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.MIN;
        };
    }

    public static LocalDateTime getCurrentDate() {
        return LocalDateTime.now();
    }
}