package com.exchange.event.domain;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.netflix.discovery.provider.Serializer;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Serializer
@JsonDeserialize
public class EventInfoMessage {
    private String tag;
    private String title;
    private String serviceName;
    private boolean persistent;
    private LocalDateTime createDate;
    private Object event;
}
