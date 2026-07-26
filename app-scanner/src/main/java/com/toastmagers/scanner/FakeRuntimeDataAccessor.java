package com.toastmagers.scanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A mock/fake implementation of {@link RuntimeDataAccessor} for testing.
 */
public class FakeRuntimeDataAccessor implements RuntimeDataAccessor {
    private final List<String> installedPackages = new ArrayList<>();
    private final List<String> activeChannelPackages = new ArrayList<>();
    private final Set<String> notificationPermissions = new HashSet<>();

    public void addInstalledPackage(String packageName, boolean hasActiveChannel, boolean hasPermission) {
        installedPackages.add(packageName);
        if (hasActiveChannel) {
            activeChannelPackages.add(packageName);
        }
        if (hasPermission) {
            notificationPermissions.add(packageName);
        }
    }

    @Override
    public List<String> getInstalledPackages() {
        return installedPackages;
    }

    @Override
    public List<String> getPackagesWithActiveChannels() {
        return activeChannelPackages;
    }

    @Override
    public boolean hasPostNotificationPermission(String packageName) {
        return notificationPermissions.contains(packageName);
    }
}
