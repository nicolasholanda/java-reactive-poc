package com.github.nicolasholanda.java_reactive_poc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/reactive")
public class ReactiveBasicsController {

    @GetMapping("/mono")
    public Mono<String> getSingleItem() {
        return Mono.just("Hello Reactive World!")
                .delayElement(Duration.ofSeconds(1));
    }
}
