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

    @GetMapping("/mono/{name}")
    public Mono<String> getPersonalizedGreeting(@PathVariable String name) {
        return Mono.just(name)
                .map(n -> "Hello " + n + "!")
                .filter(greeting -> !name.isEmpty())
                .switchIfEmpty(Mono.just("Hello Anonymous!"));
    }

    @GetMapping("/flux/numbers")
    public Flux<Integer> getNumbers() {
        return Flux.range(1, 10)
                .delayElements(Duration.ofMillis(500))
                .map(i -> i * i);
    }

    @GetMapping("/flux/fruits")
    public Flux<String> getFruits() {
        List<String> fruits = List.of("apple", "banana", "orange", "grape", "mango");
        return Flux.fromIterable(fruits)
                .delayElements(Duration.ofMillis(300))
                .map(String::toUpperCase)
                .filter(fruit -> fruit.length() > 5);
    }

    @GetMapping("/flux/async")
    public Flux<String> getAsyncData() {
        return Flux.just("Processing", "Data", "Asynchronously")
                .flatMap(word ->
                        Mono.just(word + " - processed at " + System.currentTimeMillis())
                                .delayElement(Duration.ofSeconds(1))
                );
    }

    @GetMapping("/mono/error")
    public Mono<String> getWithErrorHandling() {
        return Mono.fromCallable(() -> {
                    if (Math.random() > 0.5) {
                        throw new RuntimeException("Random error occurred!");
                    }
                    return "Success!";
                })
                .onErrorReturn("Error handled gracefully");
    }

    @GetMapping("/flux/transform")
    public Flux<String> getTransformedData() {
        return Flux.range(1, 5)
                .map(i -> "Item-" + i)
                .take(3)
                .doOnNext(item -> System.out.println("Processing: " + item))
                .delayElements(Duration.ofMillis(400));
    }
}
