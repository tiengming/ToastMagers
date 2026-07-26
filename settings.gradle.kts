rootProject.name = "ToastMagers"

// Always included subprojects
include(":app-scanner")
include(":config-system")
include(":hook-engine")
include(":performance-opt")
include(":permission-sync")
include(":release-engineering")
include(":rule-engine")
include(":security-hardening")
include(":stats-engine")
include(":webui")

// Conditionally include testing-framework only if the directory exists
if (file("testing-framework").exists()) {
    include(":testing-framework")
}
