plugins {
    base
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    // Only apply java-library to JVM modules
    if (name != "webui" && name != "release-engineering") {
        apply(plugin = "java-library")

        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        dependencies {
            "testImplementation"("junit:junit:4.13.2")
        }
    }
}

tasks.register("zipModule") {
    group = "distribution"
    description = "Generates the release Magisk module ZIP."
    dependsOn(":release-engineering:zipModule")
}
