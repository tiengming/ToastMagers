plugins {
    base
}

tasks.register<Zip>("zipModule") {
    group = "distribution"
    description = "Packages the Magisk module into a flashable ZIP."
    archiveFileName.set("ToastMagers-release.zip")
    destinationDirectory.set(file("${rootDir}/out"))

    // Include standard Magisk module template files
    from("magisk_template") {
        include("**/*")
    }

    // Include webui assets
    from("${rootDir}/webui/webroot") {
        into("webroot")
    }
}

tasks.named("clean") {
    doLast {
        delete("${rootDir}/out")
    }
}
