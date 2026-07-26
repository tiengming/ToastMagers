package com.toastmagers.sync;

import java.util.HashMap;
import java.util.Map;

/**
 * A mock/fake implementation of {@link NotificationServiceAccessor} for testing.
 */
public class FakeNotificationServiceAccessor implements NotificationServiceAccessor {
    private final Map<String, Boolean> notificationStates = new HashMap<>();

    private final Map<String, Boolean> channelStates = new HashMap<>();

    @Override
    public boolean areNotificationsEnabled(String packageName) {
        return notificationStates.getOrDefault(packageName, true);
    }

    @Override
    public boolean setNotificationsEnabled(String packageName, boolean enabled) {
        notificationStates.put(packageName, enabled);
        return true;
    }

    @Override
    public boolean isNotificationChannelEnabled(String packageName, String channelId) {
        String key = packageName + ":" + channelId;
        return channelStates.getOrDefault(key, true);
    }

    @Override
    public boolean setNotificationChannelEnabled(String packageName, String channelId, boolean enabled) {
        String key = packageName + ":" + channelId;
        channelStates.put(key, enabled);
        return true;
    }
}
