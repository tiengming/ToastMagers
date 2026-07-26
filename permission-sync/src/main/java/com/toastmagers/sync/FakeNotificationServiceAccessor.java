package com.toastmagers.sync;

import java.util.HashMap;
import java.util.Map;

/**
 * A mock/fake implementation of {@link NotificationServiceAccessor} for testing.
 */
public class FakeNotificationServiceAccessor implements NotificationServiceAccessor {
    private final Map<String, Boolean> notificationStates = new HashMap<>();

    @Override
    public boolean areNotificationsEnabled(String packageName) {
        return notificationStates.getOrDefault(packageName, true);
    }

    @Override
    public boolean setNotificationsEnabled(String packageName, boolean enabled) {
        notificationStates.put(packageName, enabled);
        return true;
    }
}
