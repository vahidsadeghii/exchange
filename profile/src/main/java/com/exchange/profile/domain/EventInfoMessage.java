package com.exchange.profile.domain;

import com.netflix.discovery.provider.Serializer;
import lombok.*;

@Getter
@Setter
@Builder
@Serializer
@AllArgsConstructor
@NoArgsConstructor
public class EventInfoMessage {
    private String tag;
    private String title;
    private String serviceName;
    private boolean persistent;
    private Object event;
}
