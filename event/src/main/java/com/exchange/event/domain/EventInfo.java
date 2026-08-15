package com.exchange.event.domain;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventInfo {
    @Id
    private String id;
    private String tag;
    private String title;
    private String serviceName;
    private Object event;
    private LocalDateTime createDate;
}
