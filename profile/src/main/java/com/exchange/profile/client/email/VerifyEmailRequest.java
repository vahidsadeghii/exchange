package com.exchange.profile.client.email;

import java.time.LocalDateTime;
import lombok.*;

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
