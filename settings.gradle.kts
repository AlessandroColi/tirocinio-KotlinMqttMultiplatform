pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.16.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "mqttMultiplatform"
include("src:commonMain")
findProject(":src:commonMain")?.name = "commonMain"
include("src:jvmMain")
findProject(":src:jvmMain")?.name = "jvmMain"
include("src:jsMain")
findProject(":src:jsMain")?.name = "jsMain"
include("src:nativeMain")
findProject(":src:nativeMain")?.name = "nativeMain"