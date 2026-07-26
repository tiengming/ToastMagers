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
}
