package com.github.nicolasholanda.java_reactive_poc.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@RestController
@RequestMapping("/backpressure")
public class BackpressureController {

    @GetMapping(value = "/buffer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressureBuffer() {
        return Flux.range(1, 100)
                .delayElements(Duration.ofMillis(10))
                .onBackpressureBuffer(20,
                        dropped -> System.out.println("Dropped: " + dropped),
                        BufferOverflowStrategy.DROP_LATEST)
                .map(i -> "Buffered item: " + i)
                .take(30);
    }

    @GetMapping(value = "/drop", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressureDrop() {
        return Flux.range(1, 100)
                .delayElements(Duration.ofMillis(10))
                .onBackpressureDrop(dropped ->
                    System.out.println("Dropping item: " + dropped))
                .map(i -> "Item: " + i + " (some may be dropped)")
                .take(30);
    }

    @GetMapping(value = "/latest", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressureLatest() {
        return Flux.range(1, 100)
                .delayElements(Duration.ofMillis(10))
                .onBackpressureLatest()
                .map(i -> "Latest item: " + i)
                .take(30);
    }

    @GetMapping(value = "/error", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressureError() {
        return Flux.range(1, 1000)
                .delayElements(Duration.ofMillis(1))
                .onBackpressureError()
                .map(i -> "Item: " + i)
                .onErrorResume(e -> Flux.just("Error: Backpressure limit exceeded - " + e.getMessage()));
    }

    @GetMapping(value = "/slow-consumer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> slowConsumerExample() {
        return Flux.interval(Duration.ofMillis(10))
                .onBackpressureBuffer(50)
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "Slow processed: " + i;
                })
                .take(20);
    }

    @GetMapping(value = "/fast-producer", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> fastProducerSlowConsumer() {
        return Flux.range(1, 50)
                .delayElements(Duration.ofMillis(5))
                .onBackpressureBuffer(10,
                    dropped -> System.out.println("Buffer full, dropped: " + dropped),
                    BufferOverflowStrategy.DROP_OLDEST)
                .publishOn(Schedulers.boundedElastic())
                .delayElements(Duration.ofMillis(100))
                .map(i -> "Processed with delay: " + i);
    }

    @GetMapping(value = "/limitrate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> limitRateExample() {
        return Flux.range(1, 100)
                .delayElements(Duration.ofMillis(10))
                .limitRate(10)
                .map(i -> "Rate-limited item: " + i)
                .take(30);
    }

    @GetMapping(value = "/sample", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sampleBackpressure() {
        return Flux.interval(Duration.ofMillis(10))
                .sample(Duration.ofMillis(200))
                .map(i -> "Sampled item: " + i)
                .take(20);
    }

    @GetMapping(value = "/window", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> windowBackpressure() {
        return Flux.range(1, 50)
                .delayElements(Duration.ofMillis(20))
                .window(10)
                .flatMap(window -> window
                    .collectList()
                    .map(list -> "Window batch: " + list))
                .onBackpressureBuffer();
    }

    @GetMapping(value = "/comparison", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> backpressureComparison() {
        Flux<Integer> fastPublisher = Flux.range(1, 100)
                .delayElements(Duration.ofMillis(5));

        return Flux.concat(
                Flux.just("=== Testing DROP strategy ==="),
                fastPublisher
                    .onBackpressureDrop()
                    .take(10)
                    .map(i -> "DROP: " + i),

                Flux.just("=== Testing BUFFER strategy ==="),
                fastPublisher
                    .onBackpressureBuffer(20)
                    .take(10)
                    .map(i -> "BUFFER: " + i),

                Flux.just("=== Testing LATEST strategy ==="),
                fastPublisher
                    .onBackpressureLatest()
                    .take(10)
                    .map(i -> "LATEST: " + i)
        ).delayElements(Duration.ofMillis(100));
    }

    @GetMapping(value = "/overflow-strategy", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> bufferOverflowStrategies() {
        return Flux.concat(
                Flux.just("=== DROP_LATEST: Keeps oldest items ==="),
                createFastFlux()
                    .onBackpressureBuffer(10,
                        d -> {},
                        BufferOverflowStrategy.DROP_LATEST)
                    .take(5)
                    .map(i -> "DROP_LATEST: " + i),

                Flux.just("=== DROP_OLDEST: Keeps newest items ==="),
                createFastFlux()
                    .onBackpressureBuffer(10,
                        d -> {},
                        BufferOverflowStrategy.DROP_OLDEST)
                    .take(5)
                    .map(i -> "DROP_OLDEST: " + i)
        ).delayElements(Duration.ofMillis(150));
    }

    private Flux<Integer> createFastFlux() {
        return Flux.range(1, 100)
                .delayElements(Duration.ofMillis(5));
    }
}
