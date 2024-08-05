package com.inconcert.domain.post.util;

import java.time.LocalDateTime;

public class DateUtil {
    public static LocalDateTime getEndDate(String period) {
        switch (period) {
            case "1day":
                return LocalDateTime.now().plusDays(1);
            case "1week":
                return LocalDateTime.now().plusWeeks(1);
            case "1month":
                return LocalDateTime.now().plusMonths(1);
            case "6months":
                return LocalDateTime.now().plusMonths(6);
            case "1year":
                return LocalDateTime.now().plusYears(1);
            default:
                return LocalDateTime.MIN;
        }
    }

    public static LocalDateTime getCurrentDate() {
        return LocalDateTime.now();
    }
}