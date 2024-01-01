import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("io.kotest.multiplatform") version "5.8.0"
    kotlin("multiplatform") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
}

group = "mqttMultiplatform"

repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://repo.eclipse.org/content/repositories/paho-releases/")
        url = uri("https://github.com/oshai/kotlin-logging")
    }
}

val os = OperatingSystem.current()

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
            filter {
                isFailOnNoMatchingTests = false
            }
            testLogging {
                showExceptions = true
                events = setOf(
                    org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                )
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            }
        }

    }

    js(IR) {
        browser()
        nodejs()
        binaries.library()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("io.arrow-kt:arrow-core:1.2.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.slf4j:slf4j-api:1.7.5")
                implementation("io.github.oshai:kotlin-logging:5.1.1")
            }
        }

        val jvmMain by getting {
            dependencies{
                dependsOn(commonMain)
                implementation("org.eclipse.paho:org.eclipse.paho.mqttv5.client:1.2.5")
            }
        }
        val jvmTest by getting {

            dependencies {
                implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.0")
                implementation("io.kotest:kotest-assertions-core:5.8.0")
                implementation("io.kotest:kotest-runner-junit5-jvm:5.8.0")
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }

    }

    val nativeSetup: KotlinNativeTarget.() -> Unit = {
        compilations["main"].defaultSourceSet.dependsOn(sourceSets["nativeMain"])
        binaries {
            sharedLib()
            staticLib()
        }
    }

    applyDefaultHierarchyTemplate()

    linuxX64(nativeSetup)

    mingwX64(nativeSetup)

    macosX64(nativeSetup)
    macosArm64(nativeSetup)
    iosArm64(nativeSetup)
    iosSimulatorArm64(nativeSetup)
    iosX64(nativeSetup)
    watchosArm64(nativeSetup)
    watchosSimulatorArm64(nativeSetup)
    tvosArm64(nativeSetup)
    tvosSimulatorArm64(nativeSetup)

    targets.all {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
                freeCompilerArgs += listOf("-Xexpect-actual-classes")
            }
        }
    }

    // Disable cross compilation
    val excludeTargets = when {
        os.isLinux -> kotlin.targets.filterNot { "linux" in it.name }
        os.isWindows -> kotlin.targets.filterNot { "mingw" in it.name }
        os.isMacOsX -> kotlin.targets.filter { "linux" in it.name || "mingw" in it.name }
        else -> emptyList()
    }.mapNotNull { it as? KotlinNativeTarget }

    configure(excludeTargets) {
        compilations.configureEach {
            cinterops.configureEach { tasks[interopProcessingTaskName].enabled = false }
            compileTaskProvider.get().enabled = false
            tasks[processResourcesTaskName].enabled = false
        }
        binaries.configureEach { linkTask.enabled = false }
    }
}
