package com.exchange.emailmanager.service.implement;


import com.exchange.emailmanager.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
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
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "${custom-config.kafka.emailverification-input-message.topic}")
    public void verifyRegisterHandle(String message) {
        try {
            log.info("EMAIL CONSUMER RECEIVED: {}", message);


            VerifyEmailSender emailMessage =
                    objectMapper.readValue(
                            message,
                            VerifyEmailSender.class
                    );

            log.info(
                    "EMAIL EVENT DESERIALIZED: emailTo={}",
                    emailMessage.emailTo()
            );

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

    @KafkaListener(
            topics = "${custom-config.kafka.changeemailverification-input-message.topic}"
    )
    public void changeEmailVerificationHandle(String message) {

        try {

            log.info(
                    "CHANGE EMAIL VERIFICATION MESSAGE RECEIVED: {}",
                    message
            );

            VerifyEmailSender emailMessage =
                    objectMapper.readValue(
                            message,
                            VerifyEmailSender.class
                    );

            emailSenderService.changeEmailSender(
                    emailMessage.emailTo(),
                    emailMessage.verifySentEmailHistoryId(),
                    emailMessage.verificationCode(),
                    emailMessage.expiredDate()
            );

        } catch (Exception e) {

            log.error(
                    "Failed to process change email verification message: {}",
                    message,
                    e
            );
        }
    }


    @KafkaListener(
            topics = "${custom-config.kafka.forgotpassword-input-message.topic}"
    )
    public void forgotPasswordHandle(String message) {

        try {

            log.info(
                    "FORGOT PASSWORD MESSAGE RECEIVED: {}",
                    message
            );

            VerifyEmailSender emailMessage =
                    objectMapper.readValue(
                            message,
                            VerifyEmailSender.class
                    );

            emailSenderService.forgotPassword(
                    emailMessage.emailTo(),
                    emailMessage.verificationCode(),
                    emailMessage.expiredDate()
            );

        } catch (Exception e) {

            log.error(
                    "Failed to process forgot password message: {}",
                    message,
                    e
            );
        }
    }


    @KafkaListener(topics = "${custom-config.kafka.sendmessage-input-message.topic}")
    public void sendMessageHandle(String message) {

        try {

            log.info("SEND MESSAGE RECEIVED: {}", message);

            SendMessage emailMessage =
                    objectMapper.readValue(
                            message,
                            SendMessage.class
                    );

            emailSenderService.sendMessage(
                    emailMessage.emailTo(),
                    emailMessage.message(),
                    emailMessage.subject()
            );

        } catch (Exception e) {

            log.error("Failed to process send message: {}", message, e);
        }
    }


    @KafkaListener(topics = "${custom-config.kafka.setuserstatus-input-message.topic}")
    public void setUserStatusHandle(String message) {

        try {

            log.info("SET USER STATUS MESSAGE RECEIVED: {}", message);

            SetUserStatus userStatus =
                    objectMapper.readValue(
                            message,
                            SetUserStatus.class
                    );

            emailSenderService.setUserStatus(
                    userStatus.email(),
                    userStatus.status(),
                    "Change Status"
            );

        } catch (Exception e) {

            log.error("Failed to process set user status message: {}", message, e);
        }
    }


    @KafkaListener(topics = "${custom-config.kafka.changeEmailVerifyCode-input-message.topic}")
    public void changeEmailVerifyCodeHandle(String message) {

        try {

            log.info("CHANGE EMAIL VERIFY CODE MESSAGE RECEIVED: {}", message);

            ChangeEmail changeEmail =
                    objectMapper.readValue(
                            message,
                            ChangeEmail.class
                    );

            emailSenderService.changeEmailVerifyCode(changeEmail.email(), changeEmail.message());

        } catch (Exception e) {

            log.error("Failed to process change email verify code message: {}", message, e);
        }
    }

}