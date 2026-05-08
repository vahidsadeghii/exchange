package com.exchange.profile.service.implement;

import com.exchange.profile.domain.VerifyEmailStatus;
import com.exchange.profile.domain.VerifySentEmailHistory;
import com.exchange.profile.exception.CanNotSendMoreEmailException;
import com.exchange.profile.exception.UserAlreadyExistException;
import com.exchange.profile.repository.VerifySentEmailHistoryRepository;
import com.exchange.profile.service.MessagingService;
import com.exchange.profile.service.UserProfileService;
import com.exchange.profile.service.VerifySentEmailHistoryService;
import com.exchange.profile.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class VerifySentEmailHistoryServiceImpl implements VerifySentEmailHistoryService {
    private final VerifySentEmailHistoryRepository verifySentEmailHistoryRepository;
    private final UserProfileService userProfileService;


    @Value("${custom-config.kafka.emailverification-output-message.topic}")
    private String emailVerificationTopic;

    @Value("${verify.send-email.expired-date}")
    private long expiredDate;

    @Override
    public VerifySentEmailHistory registerEmail(String email) {

        userProfileService.findUserByEmail(email).ifPresent(p -> {
            throw new UserAlreadyExistException();
        });

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        List<VerifySentEmailHistory> verifySentEmailList = verifySentEmailHistoryRepository.findAllByEmailAndCreateDateBetween(email, start, end);


        if (verifySentEmailList.size() == 3) {
            log.info("You can not login more than 3 times everyday");
            throw new CanNotSendMoreEmailException();
        }
        String verificationCode = String.valueOf(RandomUtil.generateRandomNumber(100000, 899999));

        VerifySentEmailHistory emailHistory = saveEmailHistory(verificationCode, email, null);
        //TODO: create other service for all Message from this service to email service that called EventModule
//        try{
//            authenticationEventService.save(emailVerificationTopic, objectMapper.writeValueAsString(
//                    new VerifyEmailSender(
//                            email, verificationCode, LocalDateTime.now().plusHours(expiredDate).toString())));
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }

        return emailHistory;
        // return null;
    }

    private VerifySentEmailHistory saveEmailHistory(String verificationCode, String email, String onlineUserId) {
        return verifySentEmailHistoryRepository.save(VerifySentEmailHistory.builder()
                .email(email)
                .expiredDate(LocalDateTime.now().plusHours(expiredDate))
                .verificationCode(verificationCode)
                .userId(onlineUserId)
                .status(VerifyEmailStatus.ENABLE)
                .createDate(LocalDateTime.now())
                .build());
    }

}
