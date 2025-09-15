package com.example.notificationservice.config;

import com.example.notificationservice.dto.UserEventDto;
import com.example.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void handleUserEvent(UserEventDto event) {
        String subject = "Уведомление о аккаунте";
        String text;
        if ("CREATE".equals(event.getOperation())) {
            text = "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.";
        } else if ("DELETE".equals(event.getOperation())) {
            text = "Здравствуйте! Ваш аккаунт был удалён.";
        } else {
            return;
        }
        emailService.sendEmail(event.getEmail(), subject, text);
    }
}
