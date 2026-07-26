#!/system/bin/sh
# post-fs-data.sh script for ToastMagers
# Executes in post-fs-data mode during early boot.

MODDIR=${0%/*}

# Create log and data files if they do not exist
mkdir -p /data/system/toast_magers
if [ ! -f /data/system/toast_magers/config.json ]; then
  echo '{"version":1,"settings":{"enable_toast_tracker":true,"show_toast_source_overlay":false,"log_level":"INFO"},"webui":{"enabled":true,"require_auth":false},"whitelist_packages":["com.android.systemui","com.android.phone"],"global_rules":[],"app_rules":{}}' > /data/system/toast_magers/config.json
fi

# Create standard module directory files and set permissions
mkdir -p "$MODDIR/webroot"
touch "$MODDIR/toast_magers_tracker.log"

# Grant ownership and permissions to 'system' user (uid 1000) so that system_server NMS hook and WebUI can read/write logs and configs
chown -R system:system /data/system/toast_magers
chmod -R 775 /data/system/toast_magers

chown -R system:system "$MODDIR"
chmod -R 775 "$MODDIR"

# Ensure proper SELinux context on files if necessary (restorecon)
restorecon -R /data/system/toast_magers
restorecon -R "$MODDIR"
