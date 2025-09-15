package com.example.notificationservice.controller;

import com.example.notificationservice.dto.UserEventDto;
import com.example.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/send")
    public void sendNotification(@RequestBody UserEventDto event) {
        String subject = "Уведомление о аккаунте";
        String text;
        if ("CREATE".equals(event.getOperation())) {
            text = "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.";
        } else if ("DELETE".equals(event.getOperation())) {
            text = "Здравствуйте! Ваш аккаунт был удалён.";
        } else {
            throw new IllegalArgumentException("Invalid operation");
        }
        emailService.sendEmail(event.getEmail(), subject, text);
    }
}
