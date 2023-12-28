import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
}

group = "mqttMultiplatform"

repositories {
    google()
    mavenCentral()
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
            }
        }
        val commonTest by getting {
            dependencies {
                // Add common test dependencies if needed
            }
        }
        val jvmMain by getting {
            dependsOn(commonMain)
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