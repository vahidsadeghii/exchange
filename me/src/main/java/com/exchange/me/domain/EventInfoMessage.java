package com.exchange.me.domain;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize
public class EventInfoMessage<T> {
    private String tag;
    private String destinationTopic;
    private String serviceName;
    private boolean persistent;
    private boolean routingEnabled;
    private LocalDateTime createDate;
    private T event;
}
