package com.exchange.profile.domain;


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
    private String title;
    private String serviceName;
    private boolean routingEnabled;
    private boolean persistent;
    private LocalDateTime createDate;
    private T event;
}
