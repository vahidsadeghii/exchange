package com.exchange.profile.service.implement;

import com.exchange.profile.domain.EventInfoMessage;
import com.exchange.profile.domain.VerifyEmailSender;
import com.exchange.profile.service.MessagingService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagingServiceImpl implements MessagingService {
    private final KafkaTemplate<String, Object> kafkaTemplateSendMessage;


    @Value("${custom-config.kafka.event-output-message.topic}")
    private String eventMessage;

    @Value("${custom-config.kafka.verification-output-message.topic}")
    private String verificationMessage;

    @Value("${verify.send-email.expired-date}")
    private long expiredDate;

    @Override
    public String sendEmailTest() {
                VerifyEmailSender message = new VerifyEmailSender(
                        "email", "445555",
                        "545555",
                        LocalDateTime.now().plusDays(expiredDate).toString());

        //eventMessage
        EventInfoMessage eventInfoMessage = EventInfoMessage.builder()
                .tag("EMAIL_VERIFICATION")
                .destinationTopic(verificationMessage)
                .serviceName("verify-sent-email-history")
                .routingEnabled(true)
                .persistent(true)
                .createDate(LocalDateTime.now())
                .event(message)
                .build();
        kafkaTemplateSendMessage.send(eventMessage, eventInfoMessage)
                .whenComplete((r, e) -> {
                    if (e != null)
                        log.error("send error", e);
                    else
                        log.info("message sent");
                });
        return "";
    }
}