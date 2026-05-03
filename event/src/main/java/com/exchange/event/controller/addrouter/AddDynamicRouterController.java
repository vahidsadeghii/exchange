package com.exchange.event.controller.addrouter;


import com.exchange.event.domain.TagRouter;
import com.exchange.event.service.TagRouterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AddDynamicRouterController {
    private final TagRouterService tagRouterService;


    @PostMapping(value = "/internal/router")
    public TagRouterResponse handle(@RequestBody TagRouterRequest request){
        TagRouter router = tagRouterService.save(request.getTag(), request.getTitleTopic());

        return TagRouterResponse.builder()
                .tag(router.getTag())
                .titleTopic(request.getTitleTopic())
                .build();
    }
}
