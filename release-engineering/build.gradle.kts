plugins {
    base
}

tasks.register<Zip>("zipModule") {
    group = "module"
    description = "Create Magisk module ZIP"

    archiveFileName.set("ToastMagers-release.zip")
    destinationDirectory.set(rootProject.layout.projectDirectory.dir("out").asFile)

    // Copy all files from root module/ directory
    from(rootProject.projectDir.resolve("module")) {
        include("module.prop")
        include("*.sh")
        include("system/**")
        include("META-INF/**")
    }

    // Include webui assets
    from(rootProject.projectDir.resolve("webui/webroot")) {
        into("webroot")
    }

    includeEmptyDirs = false
}

tasks.named("clean") {
    doLast {
        delete(rootProject.projectDir.resolve("out"))
    }
}

// Ensure zipModule runs when assemble is triggered
tasks.named("assemble") {
    dependsOn("zipModule")
}
