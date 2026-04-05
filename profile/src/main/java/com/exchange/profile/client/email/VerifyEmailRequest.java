package com.exchange.profile.client.email;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyEmailRequest {
    private String emailTo;
    private String verificationCode;
    private String link;
    private LocalDateTime expiredDate;
}
