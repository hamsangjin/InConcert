package com.inconcert.domain.notification.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationViewController {
    @GetMapping("/notifications")
    public String showNotifications() {
        return "notification";
    }
}
