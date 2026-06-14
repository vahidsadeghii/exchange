package com.exchange.eventmanager.domain;


import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagRouter {
    @Id
    private Long id;
    private String tag;
    private String titleTopic;
}
