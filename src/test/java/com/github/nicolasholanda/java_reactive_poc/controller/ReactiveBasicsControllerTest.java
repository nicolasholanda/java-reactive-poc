package com.github.nicolasholanda.java_reactive_poc.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

@WebFluxTest(ReactiveBasicsController.class)
class ReactiveBasicsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveBasicsController controller;

    @Test
    void testGetSingleItem() {
        webTestClient.get()
                .uri("/reactive/mono")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Hello Reactive World!");
    }

    @Test
    void testGetPersonalizedGreeting() {
        webTestClient.get()
                .uri("/reactive/mono/John")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Hello John!");
    }

    @Test
    void testGetNumbers() {
        webTestClient.get()
                .uri("/reactive/flux/numbers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Integer.class)
                .hasSize(10)
                .contains(1, 4, 9, 16, 25);
    }

    @Test
    void testReactiveStreamWithStepVerifier() {
        StepVerifier.create(controller.getNumbers())
                .expectNext(1, 4, 9, 16, 25, 36, 49, 64, 81, 100)
                .verifyComplete();
    }

    @Test
    void testErrorHandling() {
        StepVerifier.create(controller.getWithErrorHandling())
                .expectNextMatches(result ->
                    result.equals("Success!") || result.equals("Error handled gracefully"))
                .verifyComplete();
    }
}
