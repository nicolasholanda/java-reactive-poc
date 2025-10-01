package com.github.nicolasholanda.java_reactive_poc.controller;

import com.github.nicolasholanda.java_reactive_poc.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/advanced")
public class AdvancedReactiveController {

    private final List<User> users = List.of(
        new User(1L, "Alice", "alice@example.com", 25),
        new User(2L, "Bob", "bob@example.com", 30),
        new User(3L, "Charlie", "charlie@example.com", 35),
        new User(4L, "Diana", "diana@example.com", 28),
        new User(5L, "Eve", "eve@example.com", 22)
    );

    @GetMapping("/users")
    public Flux<User> getAllUsers() {
        return Flux.fromIterable(users)
                .delayElements(Duration.ofMillis(200));
    }

    @GetMapping("/users/adults")
    public Flux<User> getAdultUsers() {
        return Flux.fromIterable(users)
                .filter(user -> user.getAge() >= 25)
                .delayElements(Duration.ofMillis(300));
    }

    @GetMapping("/users/{id}")
    public Mono<User> getUserById(@PathVariable Long id) {
        return Flux.fromIterable(users)
                .filter(user -> user.getId().equals(id))
                .next()
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }

    @GetMapping("/users/names")
    public Flux<String> getUserNames() {
        return Flux.fromIterable(users)
                .map(User::getName)
                .sort()
                .delayElements(Duration.ofMillis(100));
    }

    @GetMapping("/users/parallel")
    public Flux<String> processUsersInParallel() {
        return Flux.fromIterable(users)
                .parallel(2)
                .runOn(reactor.core.scheduler.Schedulers.parallel())
                .map(this::processUser)
                .sequential()
                .delayElements(Duration.ofMillis(200));
    }

    @GetMapping("/users/combine")
    public Flux<String> combineUserData() {
        Flux<String> names = Flux.fromIterable(users).map(User::getName);
        Flux<Integer> ages = Flux.fromIterable(users).map(User::getAge);

        return Flux.zip(names, ages)
                .map(tuple -> tuple.getT1() + " is " + tuple.getT2() + " years old")
                .delayElements(Duration.ofMillis(300));
    }

    @GetMapping("/users/statistics")
    public Mono<String> getUserStatistics() {
        return Flux.fromIterable(users)
                .map(User::getAge)
                .reduce(Integer::sum)
                .map(totalAge -> "Total users: " + users.size() + ", Average age: " + (totalAge / users.size()));
    }

    private String processUser(User user) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Processed: " + user.getName();
    }
}
