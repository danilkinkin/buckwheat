buildscript {
    repositories {
        google()
        mavenCentral()

        maven { url=uri("https://androidx.dev/snapshots/builds/-/artifacts/repository") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.44")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:6.11.0")
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
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
