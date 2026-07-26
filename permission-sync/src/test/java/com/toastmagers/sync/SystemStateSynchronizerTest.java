package com.toastmagers.sync;

import org.junit.Assert;
import org.junit.Test;

public class SystemStateSynchronizerTest {

    @Test
    public void testSynchronization() {
        FakeNotificationServiceAccessor fakeAccessor = new FakeNotificationServiceAccessor();
        SystemStateSynchronizer synchronizer = new SystemStateSynchronizer(fakeAccessor);

        String pkg = "com.rogue.app";

        // Default should be enabled
        Assert.assertTrue(fakeAccessor.areNotificationsEnabled(pkg));

        // Synchronize block all (should disable system settings)
        boolean sync1 = synchronizer.synchronizeState(pkg, true);
        Assert.assertTrue(sync1);
        Assert.assertFalse(fakeAccessor.areNotificationsEnabled(pkg));

        // Synchronize allow all (should enable system settings)
        boolean sync2 = synchronizer.synchronizeState(pkg, false);
        Assert.assertTrue(sync2);
        Assert.assertTrue(fakeAccessor.areNotificationsEnabled(pkg));
    }

    @Test
    public void testChannelSynchronization() {
        FakeNotificationServiceAccessor fakeAccessor = new FakeNotificationServiceAccessor();
        SystemStateSynchronizer synchronizer = new SystemStateSynchronizer(fakeAccessor);

        String pkg = "com.rogue.app";
        String ch1 = "marketing";
        String ch2 = "promotions";

        Assert.assertTrue(fakeAccessor.isNotificationChannelEnabled(pkg, ch1));
        Assert.assertTrue(fakeAccessor.isNotificationChannelEnabled(pkg, ch2));

        boolean syncChannel = synchronizer.synchronizeChannelState(pkg, java.util.Arrays.asList(ch1, ch2));
        Assert.assertTrue(syncChannel);

        Assert.assertFalse(fakeAccessor.isNotificationChannelEnabled(pkg, ch1));
        Assert.assertFalse(fakeAccessor.isNotificationChannelEnabled(pkg, ch2));
    }
}
