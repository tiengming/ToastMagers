package com.toastmagers.stats;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * High-performance, thread-safe statistics manager for notification & toast intercepts (Epic K / T-STAT-01).
 * Designed for system_server execution with zero locks on critical hook paths.
 */
public class StatsManager {

    private final AtomicLong totalIntercepts = new AtomicLong(0);
    private final AtomicLong totalAllowed = new AtomicLong(0);
    private final AtomicLong totalToastIntercepts = new AtomicLong(0);
    private final AtomicLong totalNotificationIntercepts = new AtomicLong(0);

    private final ConcurrentHashMap<String, AtomicLong> appInterceptCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> channelInterceptCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> dailyInterceptCounts = new ConcurrentHashMap<>();

    private static final StatsManager INSTANCE = new StatsManager();

    public static StatsManager getInstance() {
        return INSTANCE;
    }

    public StatsManager() {
    }

    /**
     * Records a notification or toast intercept event without blocking.
     *
     * @param packageName Package name of the app
     * @param channelId   Notification channel ID (or null/empty for Toast)
     * @param type        "TOAST" or "NOTIFICATION"
     */
    public void recordIntercept(String packageName, String channelId, String type) {
        if (packageName == null || packageName.isEmpty()) {
            packageName = "unknown";
        }
        totalIntercepts.incrementAndGet();

        if ("TOAST".equalsIgnoreCase(type)) {
            totalToastIntercepts.incrementAndGet();
        } else {
            totalNotificationIntercepts.incrementAndGet();
        }

        // App-level counter
        appInterceptCounts.computeIfAbsent(packageName, k -> new AtomicLong(0)).incrementAndGet();

        // Channel-level counter
        if (channelId != null && !channelId.isEmpty()) {
            String key = packageName + ":" + channelId;
            channelInterceptCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        }

        // Daily counter
        String dateKey = LocalDate.now().toString();
        dailyInterceptCounts.computeIfAbsent(dateKey, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * Records a notification/toast allowed (passthrough) event.
     *
     * @param packageName Package name
     */
    public void recordPassThrough(String packageName) {
        totalAllowed.incrementAndGet();
    }

    public long getTotalIntercepts() {
        return totalIntercepts.get();
    }

    public long getTotalAllowed() {
        return totalAllowed.get();
    }

    public long getTotalToastIntercepts() {
        return totalToastIntercepts.get();
    }

    public long getTotalNotificationIntercepts() {
        return totalNotificationIntercepts.get();
    }

    public long getAppInterceptCount(String packageName) {
        AtomicLong count = appInterceptCounts.get(packageName);
        return count != null ? count.get() : 0;
    }

    /**
     * Gets top N blocked applications sorted by intercept count descending.
     */
    public Map<String, Long> getTopBlockedApps(int limit) {
        return appInterceptCounts.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Gets daily intercept breakdown map.
     */
    public Map<String, Long> getDailyStats() {
        Map<String, Long> result = new LinkedHashMap<>();
        dailyInterceptCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> result.put(e.getKey(), e.getValue().get()));
        return result;
    }

    /**
     * Resets all statistic counters.
     */
    public void reset() {
        totalIntercepts.set(0);
        totalAllowed.set(0);
        totalToastIntercepts.set(0);
        totalNotificationIntercepts.set(0);
        appInterceptCounts.clear();
        channelInterceptCounts.clear();
        dailyInterceptCounts.clear();
    }
}
