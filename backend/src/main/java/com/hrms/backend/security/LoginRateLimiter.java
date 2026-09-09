package com.hrms.backend.security;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class LoginRateLimiter {
    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;

    public LoginRateLimiter() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
            attempts::clear, 15, 15, TimeUnit.MINUTES
        );
    }

    public boolean isAllowed(String email) {
        return attempts.getOrDefault(email, 0) < MAX_ATTEMPTS;
    }

    public void recordFailedAttempt(String email) {
        attempts.merge(email, 1, Integer::sum);
    }

    public void resetAttempts(String email) {
        attempts.remove(email);
    }
}
