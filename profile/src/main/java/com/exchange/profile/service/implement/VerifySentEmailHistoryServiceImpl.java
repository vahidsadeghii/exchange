package com.exchange.profile.service.implement;

import com.exchange.profile.domain.*;
import com.exchange.profile.exception.*;
import com.exchange.profile.repository.VerifySentEmailHistoryRepository;
import com.exchange.profile.service.UserProfileService;
import com.exchange.profile.service.VerifySentEmailHistoryService;
import com.exchange.profile.util.MapToToken;
import com.exchange.profile.util.RandomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class VerifySentEmailHistoryServiceImpl implements VerifySentEmailHistoryService {
    private final VerifySentEmailHistoryRepository verifySentEmailHistoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplateSendMessage;
    private final UserProfileService userProfileService;
    private final TokenService tokenService;

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
        sendKafkaEvent(email, verifySentEmailHistory.getId().toString(), verificationCode, now);
        return verifySentEmailHistory;
    }

    @Override
    public TokenResponse verifyEmailCode(String verifyCodeId, String verifyCode, LocalDateTime expiredDate) {
        UUID id;
        try {
            id = UUID.fromString(verifyCodeId);
        } catch (IllegalArgumentException e) {
            throw new InvalidVerificationCodeException();
        }

        VerifySentEmailHistory history = verifySentEmailHistoryRepository.findById(id)
                .orElseThrow(NotFoundVerificationCodeException::new);

        if (history.getExpiredDate().isBefore(LocalDateTime.now())) {
            throw new VerificationCodeExpiredException();
        }
        if (Boolean.TRUE.equals(history.isUsed())) {
            throw new VerificationCodeAlreadyUsedException();
        }
        if (!history.getVerificationCode().equals(verifyCode)) {
            history.setTryCount(history.getTryCount() + 1);
            verifySentEmailHistoryRepository.save(history);
            throw new InvalidVerificationCodeException();
        }

        history.setUsed(true);
        history.setStatus(VerifyEmailStatus.ENABLE);
        history.setLastModifiedDate(LocalDateTime.now());
        verifySentEmailHistoryRepository.save(history);

        UserProfile userProfile = userProfileService.createUser(history.getEmail());
        JwtToken jwtToken = tokenService.createUser(userProfile.getEmail(), userProfile.getId());

        return MapToToken.mapToTokenResponse(jwtToken);
    }

    @Override
    public void deleteUserByEmail(String email) {
        tokenService.deleteKeycloakUserByEmail(email);
        userProfileService.deleteUserByEmail(email);
        verifySentEmailHistoryRepository.deleteAllByEmail(email);
    }

    private void sendKafkaEvent(String email, String id, String code, LocalDateTime now) {
        VerifyEmailSender message = new VerifyEmailSender(email, id, code, now.plusDays(expiredDate).toString());

        //eventMessage
        EventInfoMessage eventInfoMessage = EventInfoMessage.builder()
                .tag(eventMessage)
                .destinationTopic("event-verify-email")
                .serviceName("verify-sent-email-history")
                .routingEnabled(true)
                .persistent(true)
                .createDate(LocalDateTime.now())
                .event(message)
                .build();
        kafkaTemplateSendMessage.send(verificationMessage, eventInfoMessage)
                .whenComplete((r, e) -> {
                    if (e != null)
                        log.error("send error", e);
                    else
                        log.info("message sent");
                });
    }
}
