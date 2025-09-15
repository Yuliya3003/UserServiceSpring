package com.example.notificationservice;

import com.example.notificationservice.dto.UserEventDto;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"user-events"})
@ActiveProfiles("test")
class NotificationIntegrationTest {

    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.1"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        kafkaContainer.start();
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);

        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> 3025);
        registry.add("spring.mail.username", () -> "test");
        registry.add("spring.mail.password", () -> "test");
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
    }

    @Autowired
    private KafkaTemplate<String, UserEventDto> kafkaTemplate;

    private GreenMail greenMail;

    @BeforeEach
    void setUp() {

        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();

    }

    @AfterEach
    void tearDown() {
        if (greenMail != null) {
            greenMail.stop();
        }
    }

    @Test
    void testCreateUserEvent_sendsEmail() throws Exception {
        UserEventDto event = new UserEventDto();
        event.setOperation("CREATE");
        event.setEmail("test@example.com");

        kafkaTemplate.send("user-events", event);


        greenMail.waitForIncomingEmail(10000, 1);
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals("test@example.com", messages[0].getAllRecipients()[0].toString());
        assertEquals("Уведомление о аккаунте", messages[0].getSubject());

        assertTrue(messages[0].getContent().toString().contains("Ваш аккаунт на сайте ваш сайт был успешно создан"));
    }

    @Test
    void testDeleteUserEvent_sendsEmail() throws Exception {
        UserEventDto event = new UserEventDto();
        event.setOperation("DELETE");
        event.setEmail("test@example.com");

        kafkaTemplate.send("user-events", event);


        greenMail.waitForIncomingEmail(10000, 1);
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals("test@example.com", messages[0].getAllRecipients()[0].toString());
        assertEquals("Уведомление о аккаунте", messages[0].getSubject());

        assertTrue(messages[0].getContent().toString().contains("Ваш аккаунт был удалён"));
    }
}
