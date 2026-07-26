package com.toastmagers.security;

import com.toastmagers.rule.SafeRegexMatcher;
import org.junit.Assert;
import org.junit.Test;

public class ReDosRegressionTest {

    @Test
    public void testReDosPathologicalPatterns() {
        // Known CVE / vulnerability pattern types (Exponential Backtracking)
        String[] pathologicalPatterns = {
            "(a+)+b",
            "(a|a)+b",
            "a*a*a*a*a*a*b",
            "(a+)*b",
            "([a-zA-Z]+)*b"
        };

        String pathologicalInput = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

        for (String regex : pathologicalPatterns) {
            long start = System.currentTimeMillis();
            boolean matchResult = SafeRegexMatcher.matches(regex, pathologicalInput, 30); // 30ms limit
            long elapsed = System.currentTimeMillis() - start;

            Assert.assertFalse("Must not match " + regex, matchResult);
            // Ensure timeout protects the CPU and completes execution within a short duration
            Assert.assertTrue("Should terminate ReDoS input within 300ms limit. Regex: " + regex + " took " + elapsed + "ms", elapsed < 300);
        }
    }
}
