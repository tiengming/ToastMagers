package com.toastmagers.sync;

import org.junit.Assert;
import org.junit.Test;

public class NotificationServiceAccessorTest {

    @Test
    public void testFakeNotificationServiceAccessor() {
        FakeNotificationServiceAccessor accessor = new FakeNotificationServiceAccessor();
        String packageName = "com.example.rogueapp";

        // Default should be true
        Assert.assertTrue(accessor.areNotificationsEnabled(packageName));

        // Disable notifications
        boolean success = accessor.setNotificationsEnabled(packageName, false);
        Assert.assertTrue(success);
        Assert.assertFalse(accessor.areNotificationsEnabled(packageName));
    }

    @Test
    public void testAndroidNotificationServiceAccessorFallback() {
        // Test that the Android production class fails-open or returns gracefully when run with dummy systems
        AndroidNotificationServiceAccessor accessor = new AndroidNotificationServiceAccessor(null);
        String packageName = "com.example.rogueapp";

        Assert.assertTrue(accessor.areNotificationsEnabled(packageName));
        Assert.assertFalse(accessor.setNotificationsEnabled(packageName, false));
        Assert.assertTrue(accessor.isNotificationChannelEnabled(packageName, "channel_1"));
        Assert.assertFalse(accessor.setNotificationChannelEnabled(packageName, "channel_1", false));
    }
}
