import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "LevelUp-Soul"
            isStatic = true
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "LevelUp-Soul"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "LevelUp-Soul.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

android {
    namespace = "fireforestsoul.levelupsoul"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    lint {
        checkReleaseBuilds = false
    }
    defaultConfig {
        applicationId = "fireforestsoul.levelupsoul"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        setProperty("archivesBaseName", "LevelUpSoul-alpha$versionName")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file("src/androidMain/keystore.jks")
            storePassword = "xxx"
            keyAlias = "xxx"
            keyPassword = "xxx"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "fireforestsoul.levelupsoul.MainKt"

        nativeDistributions {
            targetFormats(
                *when (org.gradle.internal.os.OperatingSystem.current()) {
                    org.gradle.internal.os.OperatingSystem.LINUX -> arrayOf(
                        TargetFormat.Deb,
                        TargetFormat.AppImage,
                    )

                    org.gradle.internal.os.OperatingSystem.MAC_OS -> arrayOf(
                        TargetFormat.Dmg,
                        TargetFormat.Pkg
                    )

                    else -> arrayOf( //Windows
                        TargetFormat.Msi,
                        TargetFormat.Exe
                    )
                }
            )

            packageName = "LevelUpSoul"
            packageVersion = "1.0.0"

            linux {
                iconFile.set(project.file("src/desktopMain/resources/app_icon.png"))
            }
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/app_icon.icns"))
            }
            windows {
                iconFile.set(project.file("src/desktopMain/resources/app_icon.ico"))
            }
        }
    }
}

compose.resources {
    packageOfResClass = "fireforestsoul.levelupsoul"
}