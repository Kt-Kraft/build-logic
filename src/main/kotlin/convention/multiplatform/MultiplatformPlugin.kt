package convention.multiplatform

import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
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

public open class MultiplatformPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseConventionPlugin() {

  private val multiplatformOptionsExtension: MultiplatformOptionsExtension
    get() = conventionOptions.extensions.multiplatformOptions

  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_KOTLIN_MULTIPLATFORM,
      errorMessage = "Kotlin Multiplatform Gradle Plugin not found.",
    )
    applyPlugins(PLUGIN_ID_KOTLIN_MULTIPLATFORM)
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

    if (android) androidTarget {
      publishLibraryVariants("release")
      compilerOptions {
        jvmTarget.set(conventionOptions.jvmTarget)
        addDistinctCompilerArgs(Config.jvmCompilerArgs)
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

    nativeTargets.forEach { target ->
      target.binaries.framework {
        baseName = path.substring(1).replace(':', '_')
        val bundleId = path.substring(1)
          .replace(':', '.')
          .replace(Regex("-(.)")) { it.groupValues[1].uppercase() }
        freeCompilerArgs += "-Xbinary=bundleId=$bundleId"
        isStatic = true
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
