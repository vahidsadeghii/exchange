package com.exchange.me.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchEvent {
    private long id;
    private long userId;
    private String topic;
    private String eventData;
    private MatchEventStatus status;
    private Boolean processed = false;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
