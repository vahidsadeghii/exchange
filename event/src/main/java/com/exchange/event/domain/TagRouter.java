package com.exchange.event.domain;


import jakarta.persistence.Column;
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
    @Column(nullable = false)
    private String tag;

    @Column(nullable = false)
    private String destinationTopic;

    private boolean enabled;
}
