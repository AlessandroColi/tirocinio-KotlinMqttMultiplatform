import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

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
                    TestLogEvent.FAILED,
                    TestLogEvent.PASSED,
                )
                exceptionFormat = TestExceptionFormat.FULL
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
                api("io.arrow-kt:arrow-core:1.2.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                api("io.github.oshai:kotlin-logging:5.1.1")
                implementation("io.github.davidepianca98:kmqtt-common:0.4.6")
                implementation("io.github.davidepianca98:kmqtt-client:0.4.6")
            }
        }
        val commonTest by getting {

            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("io.kotest:kotest-assertions-core:5.8.0")
                implementation("io.kotest:kotest-framework-datatest:5.8.0" )
                api( "io.kotest:kotest-framework-engine:5.8.0" )
                implementation("org.jetbrains.kotlin:kotlin-test:1.9.21")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:1.9.21")
                implementation("org.jetbrains.kotlin:kotlin-test-common:1.9.21")
            }
        }
        val jvmMain by getting {
            dependencies{
                dependsOn(commonMain)
                api("org.slf4j:slf4j-simple:2.0.9")
            }
        }
        val jvmTest by getting {
            dependencies {
                dependsOn(commonTest)
                implementation("io.kotest:kotest-runner-junit5-jvm:5.8.0")
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }

        val linuxX64Main by creating {
            dependencies {
                dependsOn(nativeMain)
                implementation(files("openssl/openssl-linux-x64.klib"))
            }
        }
        val linuxX64Test by creating {
            dependencies {
                dependsOn(nativeTest)
                implementation(files("openssl/openssl-linux-x64.klib"))
            }
        }

        val tvosSimulatorArm64Main by creating {
            dependencies {
                dependsOn(nativeMain)
                implementation(files("openssl/openssl-tvos-simulator-arm64.klib"))
            }
        }
        val tvosSimulatorArm64Test by creating {
            dependencies {
                dependsOn(nativeTest)
                implementation(files("openssl/openssl-tvos-simulator-arm64.klib"))
            }
        }

        val tvosArm64Main by creating {
            dependencies {
                dependsOn(nativeMain)
                implementation(files("openssl/openssl-tvos-arm64.klib"))
            }
        }
        val tvosArm64Test by creating {
            dependencies {
                dependsOn(nativeTest)
                implementation(files("openssl/openssl-tvos-arm64.klib"))
            }
        }

        val watchosSimulatorArm64Main by creating {
            dependencies {
                dependsOn(nativeMain)
                implementation(files("openssl/openssl-watchos-simulator-arm64.klib"))
            }
        }
        val watchosSimulatorArm64Test by creating {
            dependencies {
                dependsOn(nativeTest)
                implementation(files("openssl/openssl-watchos-simulator-arm64.klib"))
            }
        }

        val watchosArm64Main by creating {
            dependencies {
                dependsOn(nativeMain)
                implementation(files("openssl/openssl-watchos-arm64.klib"))
            }
        }
        val watchosArm64Test by creating {
            dependencies {
                dependsOn(nativeTest)
                implementation(files("openssl/openssl-watchos-arm64.klib"))
            }
        }

        val iosX64Main by creating {
            dependencies {
                dependsOn(nativeMain)
                implementation(files("openssl/openssl-ios-x64.klib"))
            }
        }
        val iosX64Test by creating {
            dependencies {
                dependsOn(nativeTest)
                implementation(files("openssl/openssl-ios-x64.klib"))
            }
        }

        val iosSimulatorArm64Main by creating {
            dependencies {
                dependsOn(nativeMain)
                implementation(files("openssl/openssl-ios-simulator-arm64.klib"))
            }
        }
        val iosSimulatorArm64Test by creating {
            dependencies {
                dependsOn(nativeTest)
                implementation(files("openssl/openssl-ios-simulator-arm64.klib"))
            }
        }

        val iosArm64Main by creating {
            dependencies {
                dependsOn(nativeMain)
                implementation(files("openssl/openssl-ios-arm64.klib"))
            }
        }
        val iosArm64Test by creating {
            dependencies {
                dependsOn(nativeTest)
                implementation(files("openssl/openssl-ios-arm64.klib"))
            }
        }

        val macosArm64Main by creating {
            dependencies {
                dependsOn(nativeMain)
                implementation(files("openssl/openssl-macos-arm64.klib"))
            }
        }
        val macosArm64Test by creating {
            dependencies {
                dependsOn(nativeTest)
                implementation(files("openssl/openssl-macos-arm64.klib"))
            }
        }

        val macosX64Main by creating {
            dependencies {
                dependsOn(nativeMain)
                implementation(files("openssl/openssl-macos-x64.klib"))
            }
        }
        val macosX64Test by creating {
            dependencies {
                dependsOn(nativeTest)
                implementation(files("openssl/openssl-macos-x64.klib"))
            }
        }

        val mingwX64Main by creating {
            dependencies {
                dependsOn(nativeMain)
                implementation(files("openssl/openssl-mingw-x64.klib"))
            }
        }
        val mingwX64Test by creating {
            dependencies {
                dependsOn(nativeTest)
                implementation(files("openssl/openssl-mingw-x64.klib"))
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)
        }
        val jsTest by getting {
            dependsOn(commonTest)
        }
    }

    val nativeSetup: KotlinNativeTarget.(targetName: String) -> Unit = { targetName ->
        compilations["main"].defaultSourceSet.dependsOn(sourceSets[targetName+"Main"])
        compilations["test"].defaultSourceSet.dependsOn(kotlin.sourceSets[targetName+"Test"])
        binaries {
            sharedLib()
            staticLib()
        }
    }

    applyDefaultHierarchyTemplate()

    linuxX64 {
        nativeSetup("linuxX64")
    }

    mingwX64 {
        nativeSetup("mingwX64")
    }

    macosX64 {
        nativeSetup("macosX64")
    }

    macosArm64 {
        nativeSetup("macosArm64")
    }

    iosArm64 {
        nativeSetup("iosArm64")
    }

    iosSimulatorArm64 {
        nativeSetup("iosSimulatorArm64")
    }

    iosX64 {
        nativeSetup("iosX64")
    }

    watchosArm64 {
        nativeSetup("watchosArm64")
    }

    watchosSimulatorArm64 {
        nativeSetup("watchosSimulatorArm64")
    }

    tvosArm64 {
        nativeSetup("tvosArm64")
    }

    tvosSimulatorArm64 {
        nativeSetup("tvosSimulatorArm64")
    }


    targets.all {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
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

rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.the<NodeJsRootExtension>().download = true
}
