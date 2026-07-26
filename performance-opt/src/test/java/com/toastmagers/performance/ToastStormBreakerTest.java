package com.toastmagers.performance;

import org.junit.Assert;
import org.junit.Test;

public class ToastStormBreakerTest {

    @Test
    public void testRateLimitingAndTripping() {
        // Create a breaker: max 5 tokens, refill to max every 1000ms, 100ms cool-down
        ToastStormBreaker breaker = new ToastStormBreaker(5, 1000, 100);

        Assert.assertEquals(ToastStormBreaker.State.CLOSED, breaker.getState());

        // Use up all 5 tokens
        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(breaker.allowRequest());
        }

        // The 6th request should fail (rate limited)
        Assert.assertFalse(breaker.allowRequest());

        // The next 4 rate-limited hits should trip the breaker into OPEN state
        // stormHitsCount needs to reach maxTokens (5)
        for (int i = 0; i < 4; i++) {
            breaker.allowRequest();
        }

        // Breaker should now be in OPEN state
        Assert.assertEquals(ToastStormBreaker.State.OPEN, breaker.getState());

        // Further requests should be blocked
        Assert.assertFalse(breaker.allowRequest());
    }

    @Test
    public void testCircuitBreakerRecovery() throws InterruptedException {
        // max 2 tokens, 50ms cooldown
        ToastStormBreaker breaker = new ToastStormBreaker(2, 1000, 50);

        // Consume tokens and trip breaker
        breaker.allowRequest();
        breaker.allowRequest();
        breaker.allowRequest();
        breaker.allowRequest();

        Assert.assertEquals(ToastStormBreaker.State.OPEN, breaker.getState());
        Assert.assertFalse(breaker.allowRequest());

        // Wait for cool down duration to pass
        Thread.sleep(60);

        // Breaker should transition to HALF_OPEN / allow a trial request and transition to CLOSED
        boolean allowed = breaker.allowRequest();
        Assert.assertTrue(allowed);
        Assert.assertEquals(ToastStormBreaker.State.CLOSED, breaker.getState());
    }
}
