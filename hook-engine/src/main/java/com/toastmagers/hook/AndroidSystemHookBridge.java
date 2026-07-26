package com.toastmagers.hook;

/**
 * Production implementation of SystemHookBridge for Android system (Zygisk / native hook).
 * It dynamically loads the compiled native hook library 'toastmagers_hook' and delegates
 * hook registration to native methods.
 */
public class AndroidSystemHookBridge implements SystemHookBridge {

    static {
        try {
            System.loadLibrary("toastmagers_hook");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("AndroidSystemHookBridge: Native library 'toastmagers_hook' not found. This is normal during local JVM tests.");
        }
    }

    @Override
    public boolean hookMethod(String className, String methodName, String signature, HookCallback callback) {
        try {
            // Resolve target class via reflection first to verify existence in system_server
            Class<?> clazz = Class.forName(className);
            // Delegate to JNI native hooks (Dobby / SandHook etc.)
            return nativeHookMethod(className, methodName, signature, callback);
        } catch (Throwable t) {
            System.err.println("AndroidSystemHookBridge: Failed to hook " + className + "#" + methodName + ": " + t.getMessage());
            return false;
        }
    }

    @Override
    public boolean unhookMethod(String className, String methodName) {
        try {
            return nativeUnhookMethod(className, methodName);
        } catch (Throwable t) {
            System.err.println("AndroidSystemHookBridge: Failed to unhook " + className + "#" + methodName + ": " + t.getMessage());
            return false;
        }
    }

    // Native JNI methods to interact with Zygisk native hooks
    private native boolean nativeHookMethod(String className, String methodName, String signature, HookCallback callback);
    private native boolean nativeUnhookMethod(String className, String methodName);
}
