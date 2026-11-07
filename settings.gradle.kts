pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    // Provisions the JDK toolchain declared in app/build.gradle.kts instead of relying on
    // whatever JDK happens to be installed on the machine.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "buckwheat"
include(":app")
