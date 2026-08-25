@file:Suppress("UnstableApiUsage")

package convention.multiplatform

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import convention.android.AndroidOptionsExtension
import convention.android.androidOptions
import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_ANDROID_KMP_LIBRARY
import convention.common.constant.PLUGIN_ID_KOTLIN_MULTIPLATFORM
import convention.common.internal.applyPlugins
import convention.common.internal.requiredPlugin
import convention.common.utils.Config
import convention.common.utils.addDistinctCompilerArgs
import convention.common.utils.addDistinctOptIns
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

public open class MultiplatformPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseConventionPlugin() {

  private val multiplatformOptionsExtension: MultiplatformOptionsExtension
    get() = conventionOptions.extensions.multiplatformOptions

  private val androidOptions: AndroidOptionsExtension
    get() = conventionOptions.extensions.androidOptions

  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_KOTLIN_MULTIPLATFORM,
      errorMessage = "Kotlin Multiplatform Gradle Plugin not found.",
    )
    applyPlugins(PLUGIN_ID_KOTLIN_MULTIPLATFORM)
    if (multiplatformOptionsExtension.android.get()) {
      pluginRegistry.requiredPlugin(
        pluginId = PLUGIN_ID_ANDROID_KMP_LIBRARY,
        errorMessage = "Android Kotlin Multiplatform Library Gradle Plugin not found.",
      )
      applyPlugins(PLUGIN_ID_ANDROID_KMP_LIBRARY)
    }
    configureMultiplatform(multiplatformOptionsExtension)
  }

  @OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
  private fun Project.configureMultiplatform(
    multiplatformOptionsExtension: MultiplatformOptionsExtension
  ) = extensions.configure<KotlinMultiplatformExtension> {
    val (jvm, android, linux, iOS, js, tvOS, macOS, watchOS, windows, wasmJs, wasmWASI) =
      multiplatformOptionsExtension

    explicitApi()

    applyDefaultHierarchyTemplate()

    if (linux) {
      linuxX64()
      linuxArm64()
    }

    if (windows) mingwX64()

    if (js) js(IR) {
      browser()
      nodejs()
      binaries.library()
    }

    if (wasmJs) wasmJs {
      outputModuleName.set(this@configureMultiplatform.name)
      nodejs()
      browser()
      binaries.library()
    }

    if (wasmWASI) wasmWasi {
      nodejs()
    }

    if (android) {
      extensions.configure<KotlinMultiplatformAndroidLibraryExtension> {
        compileSdk {
          version = release(
            maxOf(
              androidOptions.compileSdk.get(),
              androidOptions.targetSdk.get()
            )
          )
        }

        minSdk = androidOptions.minSdk.get()
      }
    }

    if (jvm) jvm()

    val nativeTargets = mutableListOf<KotlinNativeTarget>()

    if (iOS) {
      nativeTargets += iosArm64()
      nativeTargets += iosSimulatorArm64()
    }

    if (macOS) {
      nativeTargets += macosArm64()
      nativeTargets += macosX64()
    }

    if (tvOS) {
      nativeTargets += tvosX64()
      nativeTargets += tvosArm64()
      nativeTargets += tvosSimulatorArm64()
    }

    if (watchOS) {
      nativeTargets += watchosX64()
      nativeTargets += watchosArm64()
      nativeTargets += watchosDeviceArm64()
      nativeTargets += watchosSimulatorArm64()
    }

    val xcf = XCFramework()
    nativeTargets.forEach { target ->
      target.binaries.framework {
        baseName = path.substring(1).replace(':', '_')
        val bundleId = path.substring(1)
          .replace(':', '.')
          .replace(Regex("-(.)")) { it.groupValues[1].uppercase() }
        freeCompilerArgs += "-Xbinary=bundleId=$bundleId"
        isStatic = true
        xcf.add(this)
      }
    }

    sourceSets.apply {
      all {
        languageSettings {
          progressiveMode = true
          Config.optIns.forEach { optIn(it) }
        }
      }
    }

    targets.all {
      compilations.all {
        compileTaskProvider.configure {
          compilerOptions {
            addDistinctCompilerArgs(Config.compilerArgs)
            addDistinctOptIns(Config.optIns)
            progressiveMode.set(true)
          }
        }
      }
    }
  }
}
