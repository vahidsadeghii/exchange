package com.exchange.eventmanager.domain;

import com.netflix.discovery.provider.Serializer;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Serializer
@AllArgsConstructor
@NoArgsConstructor
public class MessageInfo {
    private String tag;
    private String title;
    private String serviceName;
    private boolean persistent;
    private LocalDateTime createDate;
}
