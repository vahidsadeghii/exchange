package com.exchange.event.domain;


import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;


@Document
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagRouter {
      @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String tag;
    private String titleTopic;
}
