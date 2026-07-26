#!/system/bin/sh
# uninstall.sh script for ToastMagers
# Executes when the module is removed/uninstalled.

# Clean up ToastMagers configuration and persistence data from /data/system
if [ -d /data/system/toast_magers ]; then
  rm -rf /data/system/toast_magers
fi

# Clean up any external leftover log files
if [ -f /data/adb/toast_magers_tracker.log ]; then
  rm -f /data/adb/toast_magers_tracker.log
fi
