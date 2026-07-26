package com.toastmagers.rule;

import org.junit.Assert;
import org.junit.Test;

public class SafeRegexMatcherTest {

    @Test
    public void testNormalRegexMatching() {
        Assert.assertTrue(SafeRegexMatcher.matches(".*(领红包|限时特惠).*", "快来领红包吧！"));
        Assert.assertFalse(SafeRegexMatcher.matches(".*(领红包|限时特惠).*", "这是一条普通的正常通知"));
    }

    @Test
    public void testReDoSSafetyTimeout() {
        // Pathological/backtracking regex prone to ReDoS
        String pathologicalRegex = "(a+)+b";
        // Safe input
        Assert.assertTrue(SafeRegexMatcher.matches(pathologicalRegex, "aaaaab"));

        // Pathological input (many 'a's with no ending 'b' will cause exponential backtracking in standard Java Pattern)
        String pathologicalInput = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        long start = System.currentTimeMillis();
        boolean matched = SafeRegexMatcher.matches(pathologicalRegex, pathologicalInput, 50);
        long duration = System.currentTimeMillis() - start;

        // Verify it didn't hang and returned within a small window (timeout is 50ms, allow up to 500ms for GC/slowing on VM)
        Assert.assertFalse(matched);
        Assert.assertTrue("Should timeout and exit quickly. Actual duration: " + duration + "ms", duration < 500);
    }
}
