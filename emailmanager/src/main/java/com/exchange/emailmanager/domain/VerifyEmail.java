package com.exchange.emailmanager.domain;


import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@Serializer
public class VerifyEmail {
    private String emailTo;
    private String verifyCode;
    private String link;
    private Timestamp expirationDate;
}