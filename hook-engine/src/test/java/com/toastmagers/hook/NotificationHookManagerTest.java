package com.toastmagers.hook;

import org.junit.Assert;
import org.junit.Test;

public class NotificationHookManagerTest {

    @Test
    public void testHookInstallationAndFailOpen() {
        FakeSystemHookBridge fakeBridge = new FakeSystemHookBridge();
        NotificationHookManager manager = new NotificationHookManager(fakeBridge, null, null, null);

        boolean installed = manager.installHooks();
        Assert.assertTrue(installed);
        Assert.assertTrue(fakeBridge.isHooked("com.android.server.notification.NotificationManagerService", "enqueueToast"));

        // Triggering hook with invalid args should not crash the hook callback (Fail-open)
        try {
            Object result = fakeBridge.triggerHook("com.android.server.notification.NotificationManagerService", "enqueueToast", new Object[]{});
            Assert.assertNull(result); // Default return is null (continue)
        } catch (Throwable t) {
            Assert.fail("HookCallback should catch all exceptions and fail-open gracefully");
        }
    }
}
