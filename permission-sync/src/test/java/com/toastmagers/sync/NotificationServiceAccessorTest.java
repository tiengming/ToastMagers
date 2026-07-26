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
}
