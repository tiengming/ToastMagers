package com.toastmagers.hook;

import java.util.HashMap;
import java.util.Map;

/**
 * A mock/fake implementation of {@link SystemHookBridge} for testing.
 */
public class FakeSystemHookBridge implements SystemHookBridge {
    private final Map<String, HookCallback> hooks = new HashMap<>();

    @Override
    public boolean hookMethod(String className, String methodName, String signature, HookCallback callback) {
        hooks.put(className + "#" + methodName, callback);
        return true;
    }

    @Override
    public boolean unhookMethod(String className, String methodName) {
        return hooks.remove(className + "#" + methodName) != null;
    }

    public Object triggerHook(String className, String methodName, Object[] args) throws Throwable {
        HookCallback callback = hooks.get(className + "#" + methodName);
        if (callback != null) {
            return callback.onHookTriggered(args);
        }
        return null;
    }
    
    public boolean isHooked(String className, String methodName) {
        return hooks.containsKey(className + "#" + methodName);
    }
}
