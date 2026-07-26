package com.toastmagers.hook;

import org.junit.Assert;
import org.junit.Test;

public class SystemHookBridgeTest {

    @Test
    public void testFakeHookBridge() throws Throwable {
        FakeSystemHookBridge bridge = new FakeSystemHookBridge();
        String className = "com.android.server.notification.NotificationManagerService";
        String methodName = "enqueueToast";
        String signature = "(Ljava/lang/String;Landroid/os/IBinder;Ljava/lang/CharSequence;Landroid/os/IBinder;I)V";

        boolean hookAdded = bridge.hookMethod(className, methodName, signature, new SystemHookBridge.HookCallback() {
            @Override
            public Object onHookTriggered(Object[] args) {
                return "Blocked";
            }
        });

        Assert.assertTrue(hookAdded);
        Assert.assertTrue(bridge.isHooked(className, methodName));

        Object result = bridge.triggerHook(className, methodName, new Object[]{});
        Assert.assertEquals("Blocked", result);

        boolean unhooked = bridge.unhookMethod(className, methodName);
        Assert.assertTrue(unhooked);
        Assert.assertFalse(bridge.isHooked(className, methodName));
    }
}
