package com.toastmagers.hook;

/**
 * Interface representing the bridge for Hook injection (Epic A).
 * This abstracts actual ART level method hook implementations (e.g. Dobby, SandHook).
 */
public interface SystemHookBridge {
    /**
     * Installs a hook on a specified system class and method.
     *
     * @param className  The target class name in system_server
     * @param methodName The target method name to hook
     * @param signature  The JNI signature of the target method
     * @param callback   The hook callback to execute
     * @return true if hook installed successfully, false otherwise
     */
    boolean hookMethod(String className, String methodName, String signature, HookCallback callback);

    /**
     * Uninstalls a previously installed hook.
     *
     * @param className  The target class name
     * @param methodName The target method name
     * @return true if unhooked successfully, false otherwise
     */
    boolean unhookMethod(String className, String methodName);

    interface HookCallback {
        /**
         * Invoked when the hooked method is called.
         *
         * @param args The arguments passed to the method
         * @return The result or replacement value, or null to let system continue
         */
        Object onHookTriggered(Object[] args) throws Throwable;
    }
}
