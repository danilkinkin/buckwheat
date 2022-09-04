buildscript {
    repositories {
        google()
        mavenCentral()

        if (!libs.versions.compose.snapshot.get().endsWith("SNAPSHOT")) {
            maven { url = uri("https://androidx.dev/snapshots/builds/${libs.versions.compose.snapshot.get()}/artifacts/repository/") }
        }
    }
    dependencies {
        classpath(libs.android.gradlePlugin)
        classpath(libs.hilt.gradlePlugin)
        classpath(libs.kotlin.gradlePlugin)
        classpath(libs.spotless.gradlePlugin)
        classpath(libs.secrets.gradlePlugin)
    }
}

subprojects {
    repositories {
        google()
        mavenCentral()
    }

    pluginManager.apply("com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt")
            targetExclude("bin/**/*.kt")

            licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
        }
    }
}
plugins {
    id("com.github.ben-manes.versions") version "0.41.0"
    id("nl.littlerobots.version-catalog-update") version "0.6.0"
}

apply("${project.rootDir}/buildscripts/toml-updater-config.gradle")
