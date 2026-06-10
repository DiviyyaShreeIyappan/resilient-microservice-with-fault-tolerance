// Reference: https://www.youtube.com/watch?v=ouhBu-Sp3Hw
// Reference: https://docs.spring.io/spring-boot/reference/actuator/endpoints.html
// Reference: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicReference.html
// Reference: https://resilience4j.readme.io/docs/getting-started
// Reference: https://github.com/codecentric/chaos-monkey-spring-boot

package com.example.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class MessageController {

    private static final AtomicReference<Double> failureRate = new AtomicReference<>(0.0);
    private static final AtomicReference<Integer> failureStatusCode = new AtomicReference<>(500);
    private static final AtomicReference<Double> delayRate = new AtomicReference<>(0.0);
    private static final AtomicReference<Long> delayMs = new AtomicReference<>(0L);

    @GetMapping("/api/message")
    public ResponseEntity<Map<String, String>> getMessage() {
        try {
            double random = ThreadLocalRandom.current().nextDouble();
            // Reference: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ThreadLocalRandom.html
            if (random < delayRate.get()) 
            {
             Thread.sleep(delayMs.get());
            }
            if (random < failureRate.get()) 
            {
                return ResponseEntity.status(failureStatusCode.get()).body(Map.of("error", "Injected failure for testing"));
            }
            return ResponseEntity.ok(Map.of("message", "Hello from Backend (normal mode)"));

        } 
        catch (InterruptedException e) 
        {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Backend interrupted"));
        }
    }

    @PostMapping("/config/failure")
    public ResponseEntity<Map<String, Object>> setFailure(@RequestBody Map<String, Object> config) {
        double rate = ((Number) config.getOrDefault("failure_rate", 0.0)).doubleValue();
        int status = ((Number) config.getOrDefault("status_code", 500)).intValue();

        failureRate.set(rate);
        failureStatusCode.set(status);

        return ResponseEntity.ok(Map.of(
                "message", "Failure configuration updated",
                "failure_rate", rate,
                "status_code", status
        ));
    }

    @PostMapping("/config/latency")
    public ResponseEntity<Map<String, Object>> setLatency(@RequestBody Map<String, Object> config) {
        double rate = ((Number) config.getOrDefault("delay_rate", 0.0)).doubleValue();
        long delay = ((Number) config.getOrDefault("delay_ms", 0)).longValue();

        delayRate.set(rate);
        delayMs.set(delay);

        return ResponseEntity.ok(Map.of(
        "message", "Latency configuration updated",
        "delay_rate", rate,
        "delay_ms", delay
        ));
    }

    @GetMapping("/config")
    public Map<String, Object> getCurrentConfig() {
        return Map.of(
            "failure_rate", failureRate.get(),
            "status_code", failureStatusCode.get(),
            "delay_rate", delayRate.get(),
            "delay_ms", delayMs.get()
        );
    }
}
