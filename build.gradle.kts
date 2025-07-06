buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.11.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.56.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:7.0.4")
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    pluginManager.apply("com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            // by default the target is every '.kt' and '.kts` file in the java sourcesets
            ktfmt()    // has its own section below
            ktlint()   // has its own section below
            diktat()   // has its own section below
            prettier() // has its own section below
            licenseHeaderFile(rootProject.file("spotless/copyright.kt")) // or licenseHeaderFile
        }
        kotlinGradle {
            target("*.gradle.kts") // default target for kotlinGradle
            ktlint() // or ktfmt() or prettier()
        }
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.52.0"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
}
