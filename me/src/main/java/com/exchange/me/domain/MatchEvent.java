package com.exchange.me.domain;

import lombok.*;

import java.time.LocalDateTime;
import com.exchange.me.sbe.MatchStatus;

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
    private MatchStatus matchStatus;
    private Boolean processed;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
