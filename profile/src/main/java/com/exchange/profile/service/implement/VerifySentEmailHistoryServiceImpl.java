package com.exchange.profile.service.implement;

import com.exchange.profile.domain.EventInfoMessage;
import com.exchange.profile.domain.VerifyEmailSender;
import com.exchange.profile.domain.VerifyEmailStatus;
import com.exchange.profile.domain.VerifySentEmailHistory;
import com.exchange.profile.exception.CanNotSendMoreEmailException;
import com.exchange.profile.exception.UserAlreadyExistException;
import com.exchange.profile.repository.VerifySentEmailHistoryRepository;
import com.exchange.profile.service.UserProfileService;
import com.exchange.profile.service.VerifySentEmailHistoryService;
import com.exchange.profile.util.RandomUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;


@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class VerifySentEmailHistoryServiceImpl implements VerifySentEmailHistoryService {
    private final VerifySentEmailHistoryRepository verifySentEmailHistoryRepository;
    private final KafkaTemplate<String, VerifyEmailSender> kafkaTemplateSendMessage;
    private final UserProfileService userProfileService;

    @Value("${custom-config.kafka.event-output-message.topic}")
    private String eventMessage;

    @Value("${custom-config.kafka.verification-output-message.topic}")
    private String verificationMessage;

    @Value("${verify.send-email.expired-date}")
    private long expiredDate;

        private static final int MAX_ATTEMPTS_PER_DAY = 3;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public VerifySentEmailHistory registerEmail(String email) {

        userProfileService.findUserByEmail(email).ifPresent(p -> {
            throw new UserAlreadyExistException();
        });

       var now = LocalDateTime.now();
        var start = now.toLocalDate().atStartOfDay();
        var end = now.toLocalDate().atTime(LocalTime.MAX);

        List<VerifySentEmailHistory> histories = verifySentEmailHistoryRepository.findAllByEmailAndCreateDateBetween(email, start, end);

       int tryCount = histories.stream()
                .max(Comparator.comparing(VerifySentEmailHistory::getCreateDate))
                .map(VerifySentEmailHistory::getTryCount)
                .orElse(0);

        if (tryCount >= MAX_ATTEMPTS_PER_DAY) {
            log.warn("Email {} exceeded daily verification attempts", email);
            throw new CanNotSendMoreEmailException();
        }
        String verificationCode = String.valueOf(RandomUtil.generateRandomNumber(100000, 899999));

        VerifySentEmailHistory history = VerifySentEmailHistory.builder()
                .userId(null)
                .email(email)
                .verificationCode(verificationCode)
                .expiredDate(now.plusDays(expiredDate))
                .tryCount(tryCount + 1)
                .status(VerifyEmailStatus.ENABLE)
                .createDate(now)
                .lastModifiedDate(now)
                .build();

         var verifySentEmailHistory = verifySentEmailHistoryRepository.save(history);
         sendKafkaEvent(email, verifySentEmailHistory.getId(), verificationCode, now);
        return verifySentEmailHistory;
    }

       private void sendKafkaEvent(String email, String id, String code, LocalDateTime now) {
        var payload = new VerifyEmailSender(
                email,
                id,
                code,
                now.plusHours(expiredDate).toString()
        );


            var message = new VerifyEmailSender(email, id, code, now.plusHours(expiredDate).toString());
            kafkaTemplateSendMessage.send(verificationMessage, message);
            //kafkaTemplateSendMessage.send(eventMessage, message);


    }
}
