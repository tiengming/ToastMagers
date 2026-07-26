package com.toastmagers.rule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A ReDoS-safe regex matcher using a time-limiting CharSequence wrapper.
 * This intercepts charAt() calls by Java's regex engine to enforce a strict timeout,
 * preventing CPU exhaustion on pathological/backtracking regex inputs (T-RULE-01).
 */
public class SafeRegexMatcher {

    private static final long DEFAULT_TIMEOUT_MS = 100; // 100ms max per match

    public static boolean matches(String regex, String input) {
        return matches(regex, input, DEFAULT_TIMEOUT_MS);
    }

    public static boolean matches(String regex, String input, long timeoutMs) {
        if (regex == null || input == null) {
            return false;
        }
        try {
            Pattern pattern = Pattern.compile(regex);
            TimeoutCharSequence timeoutInput = new TimeoutCharSequence(input, timeoutMs);
            Matcher matcher = pattern.matcher(timeoutInput);
            return matcher.matches();
        } catch (TimeoutException | java.util.regex.PatternSyntaxException e) {
            // Safe fallback / open-fail: on timeout or syntax error, log and return false
            System.err.println("Regex match failed or timed out: " + e.getMessage());
            return false;
        }
    }

    private static class TimeoutCharSequence implements CharSequence {
        private final CharSequence inner;
        private final long timeoutTime;

        public TimeoutCharSequence(CharSequence inner, long timeoutMs) {
            this.inner = inner;
            this.timeoutTime = System.currentTimeMillis() + timeoutMs;
        }

        @Override
        public int length() {
            return inner.length();
        }

        @Override
        public char charAt(int index) {
            if (System.currentTimeMillis() > timeoutTime) {
                throw new TimeoutException("Regex evaluation exceeded timeout limit.");
            }
            return inner.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new TimeoutCharSequence(inner.subSequence(start, end), timeoutTime - System.currentTimeMillis());
        }

        @Override
        public String toString() {
            return inner.toString();
        }
    }

    public static class TimeoutException extends RuntimeException {
        public TimeoutException(String message) {
            super(message);
        }
    }
}
