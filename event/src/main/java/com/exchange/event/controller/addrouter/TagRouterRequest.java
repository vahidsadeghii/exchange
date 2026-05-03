package com.exchange.event.controller.addrouter;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagRouterRequest {
    private String tag;
    private String titleTopic;
}
