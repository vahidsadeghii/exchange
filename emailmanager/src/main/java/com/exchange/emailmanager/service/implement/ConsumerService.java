package com.exchange.emailmanager.service.implement;


import com.exchange.emailmanager.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {
    private final EmailSenderServiceImpl emailSenderService;


    @KafkaListener(topics = "${custom-config.kafka.emailverification-input-message.topic}")
    public void verifyRegisterHandle(String message) {
        try {
            log.info("EMAIL CONSUMER RECEIVED: {}", message);

            ObjectMapper objectMapper = new ObjectMapper();
            VerifyEmailSender emailMessage =
                    objectMapper.readValue(message, VerifyEmailSender.class);

            emailSenderService.mailSender(
                    emailMessage.emailTo(),
                    emailMessage.verifySentEmailHistoryId(),
                    emailMessage.verificationCode(),
                    emailMessage.expiredDate()
            );

        } catch (Exception e) {
            log.error("Failed to process email message: {}", message, e);
        }
    }

    //@RetryableTopic
    @KafkaListener(topics = "${custom-config.kafka.changeemailverification-input-message.topic}")
    public void changeEmailVerificationHandle(String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            VerifyEmailSender emailMessage = objectMapper.readValue(message, VerifyEmailSender.class);
            emailSenderService.changeEmailSender(emailMessage.emailTo(), emailMessage.verifySentEmailHistoryId() , emailMessage.verificationCode(), emailMessage.expiredDate());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    //@RetryableTopic
    @KafkaListener(topics = "${custom-config.kafka.forgotpassword-input-message.topic}")
    public void forgotPasswordHandle(String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            VerifyEmailSender emailMessage = objectMapper.readValue(message, VerifyEmailSender.class);
            emailSenderService.forgotPassword(emailMessage.emailTo(), emailMessage.verificationCode(), emailMessage.expiredDate());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    //@RetryableTopic
    @KafkaListener(topics = "${custom-config.kafka.sendmessage-input-message.topic}")
    public void sendMessageHandle(String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SendMessage emailMessage = objectMapper.readValue(message, SendMessage.class);
            emailSenderService.sendMessage(emailMessage.emailTo(), emailMessage.message(), emailMessage.subject());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    //@RetryableTopic
    @KafkaListener(topics = "${custom-config.kafka.setuserstatus-input-message.topic}")
    public void setUserStatusHandle(String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SetUserStatus userStatus = objectMapper.readValue(message, SetUserStatus.class);
            emailSenderService.setUserStatus(userStatus.email(), userStatus.status(), "Change Status");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    //@RetryableTopic
    @KafkaListener(topics = "${custom-config.kafka.changeEmailVerifyCode-input-message.topic}")
    public void changeEmailVerifyCodeHandle(String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ChangeEmail changeEmail = objectMapper.readValue(message, ChangeEmail.class);
            emailSenderService.changeEmailVerifyCode(changeEmail.email(), changeEmail.message());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
