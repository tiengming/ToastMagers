package com.toastmagers.performance;

/**
 * Implements a rate-limiter and circuit breaker to prevent Toast/Notification storms from overloading system_server (T-PERF-02).
 */
public class ToastStormBreaker {

    public enum State {
        CLOSED,     // Normal operations, passing toasts with rate-limiting
        OPEN,       // Storm detected, actively blocking all toasts
        HALF_OPEN   // Cool-down period, testing with reduced rate limits
    }

    private final int maxTokens;
    private final long refillIntervalMs;
    private final long cooldownMs;

    private State state = State.CLOSED;
    private double tokens;
    private long lastRefillTime;
    private long openStateStartTime;
    private int stormHitsCount;

    public ToastStormBreaker(int maxTokens, long refillIntervalMs, long cooldownMs) {
        this.maxTokens = maxTokens;
        this.refillIntervalMs = refillIntervalMs;
        this.cooldownMs = cooldownMs;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    /**
     * Checks if a toast is allowed, updating the rate limiter and circuit breaker state.
     *
     * @return true if allowed, false if blocked/tripped (T-PERF-02)
     */
    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        refillTokens(now);

        // State transition checks
        if (state == State.OPEN) {
            if (now - openStateStartTime > cooldownMs) {
                // Cool down complete, move to HALF_OPEN
                state = State.HALF_OPEN;
                tokens = maxTokens / 2; // half tokens for test
                System.out.println("Circuit Breaker: State transitioning from OPEN to HALF_OPEN.");
            } else {
                // Blocked in OPEN state
                return false;
            }
        }

        if (tokens >= 1.0) {
            tokens -= 1.0;
            if (state == State.HALF_OPEN) {
                // HALF_OPEN allows a small number of requests, let's gradually transition to CLOSED
                stormHitsCount = 0;
                state = State.CLOSED;
                System.out.println("Circuit Breaker: State transitioning from HALF_OPEN to CLOSED.");
            }
            return true;
        } else {
            // Out of tokens - rate limit reached
            if (state == State.CLOSED) {
                stormHitsCount++;
                if (stormHitsCount >= maxTokens) {
                    // Storm detected! Trip the circuit breaker (move to OPEN state)
                    state = State.OPEN;
                    openStateStartTime = now;
                    System.err.println("Circuit Breaker tripped! Storm detected. Actively blocking Toast/Notifications for " + cooldownMs + "ms.");
                }
            } else if (state == State.HALF_OPEN) {
                // Tripped immediately in HALF_OPEN
                state = State.OPEN;
                openStateStartTime = now;
                System.err.println("Circuit Breaker re-tripped in HALF_OPEN! Actively blocking.");
            }
            return false;
        }
    }

    private void refillTokens(long now) {
        long elapsed = now - lastRefillTime;
        if (elapsed > 0) {
            double addTokens = (double) elapsed / refillIntervalMs * maxTokens;
            tokens = Math.min(maxTokens, tokens + addTokens);
            lastRefillTime = now;
        }
    }

    public synchronized State getState() {
        return state;
    }

    public synchronized void reset() {
        this.state = State.CLOSED;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
        this.stormHitsCount = 0;
    }
}
