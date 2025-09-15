package com.example.notificationservice.controller;

import com.example.notificationservice.dto.UserEventDto;
import com.example.notificationservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendNotification_validEvent_returnsOk() throws Exception {
        UserEventDto event = new UserEventDto();
        event.setOperation("CREATE");
        event.setEmail("test@example.com");

        mockMvc.perform(post("/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operation\":\"CREATE\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isOk());
    }
}
