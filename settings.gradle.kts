@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        val properties = java.util.Properties()
        rootDir.resolve("gradle/libs.versions.toml").inputStream().use(properties::load)
        val kotlinVersion = properties.getProperty("kotlin").removeSurrounding("\"")
        eachPlugin {
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                useVersion(kotlinVersion)
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/google")
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}


includeBuild("../ComposeFilePicker") {
    dependencySubstitution {
        substitute(module("com.vickyleu.kmp.filepicker:filepicker")).using(project(":filePicker"))
    }
}

rootProject.name = "DocumentViewer"
include(":viewer")
