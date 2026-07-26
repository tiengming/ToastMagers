package com.toastmagers.scanner;

import org.junit.Assert;
import org.junit.Test;
import java.util.List;

public class RuntimeDataAccessorTest {

    @Test
    public void testFakeRuntimeDataAccessor() {
        FakeRuntimeDataAccessor accessor = new FakeRuntimeDataAccessor();
        accessor.addInstalledPackage("com.example.app1", true, true);
        accessor.addInstalledPackage("com.example.app2", false, false);

        List<String> installed = accessor.getInstalledPackages();
        Assert.assertEquals(2, installed.size());
        Assert.assertTrue(installed.contains("com.example.app1"));
        Assert.assertTrue(installed.contains("com.example.app2"));

        List<String> activeChannels = accessor.getPackagesWithActiveChannels();
        Assert.assertEquals(1, activeChannels.size());
        Assert.assertTrue(activeChannels.contains("com.example.app1"));

        Assert.assertTrue(accessor.hasPostNotificationPermission("com.example.app1"));
        Assert.assertFalse(accessor.hasPostNotificationPermission("com.example.app2"));
    }

    @Test
    public void testAndroidRuntimeDataAccessorFallback() {
        AndroidRuntimeDataAccessor accessor = new AndroidRuntimeDataAccessor(null, null);
        Assert.assertTrue(accessor.getInstalledPackages().isEmpty());
        Assert.assertTrue(accessor.getPackagesWithActiveChannels().isEmpty());
        Assert.assertFalse(accessor.hasPostNotificationPermission("com.example.app"));
    }
}
