// Reference: https://www.youtube.com/watch?v=4sL00jc5BPc
package com.example.client.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class BackendClient {
    private static final Logger log = LoggerFactory.getLogger(BackendClient.class);
    // client willl communicate to backend via this api
    @Value("${backend.url:http://backendservice:8080/api/message}")
    private String backendUrl;
    private final RestTemplate restTemplate;
    public BackendClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @Retry(name = "backendRetry")
    @CircuitBreaker(name = "backendService", fallbackMethod = "fallback")
    public Map<String, Object> callBackend() {
        log.info("Calling BackendService at {}", backendUrl);
        // Referred from : https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html
        Map<?, ?> backendResponse = restTemplate.getForObject(backendUrl, Map.class);

        return Map.of(
                "fromClient", "ClientService here (circuit closed / retry OK)",
                "backendResponse", backendResponse
        );
    }
    public Map<String, Object> fallback(Throwable throwable) {
        if (throwable instanceof CallNotPermittedException) {
            log.error("CircuitBreaker is OPEN. Failing fast without calling BackendService.");
            return Map.of(
            "fromClient", "ClientService (circuit breaker FAST-FAIL)",
            "error", "CircuitBreaker OPEN - backend temporarily disabled"
            );
        } 
        else 
        {
            log.warn("BackendService call failed after retries or due to error: {}", throwable.toString());
            return Map.of(
            "fromClient", "ClientService (retry exhausted or backend error)",
            "error", throwable.getMessage()
            );
        }
    }
}
