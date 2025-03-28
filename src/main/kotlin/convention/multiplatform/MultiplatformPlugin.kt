package convention.multiplatform

import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_KOTLIN_MULTIPLATFORM
import convention.common.internal.applyPlugins
import convention.common.internal.requiredPlugin
import convention.common.utils.Config
import convention.common.utils.requireLib
import convention.common.utils.versionCatalog
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

public open class MultiplatformPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseConventionPlugin() {

  private val multiplatformOptionsExtension: MultiplatformOptionsExtension
    get() = conventionExtension.extensions.multiplatformOptions

  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_KOTLIN_MULTIPLATFORM,
      errorMessage = "Kotlin Multiplatform Gradle Plugin not found.",
    )
    applyPlugins(PLUGIN_ID_KOTLIN_MULTIPLATFORM)
    configureMultiplatform(multiplatformOptionsExtension)
  }

  @OptIn(ExperimentalWasmDsl::class)
  private fun Project.configureMultiplatform(
    multiplatformOptionsExtension: MultiplatformOptionsExtension
  ) = extensions.configure<KotlinMultiplatformExtension> {
    val (jvm, android, linux, iOS, js, tvOS, macOS, watchOS, windows, wasmJs, wasmWASI, desktop) =
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
      moduleName = this@configureMultiplatform.name
      nodejs()
      browser()
      binaries.library()
    }

    if (wasmWASI) wasmWasi {
      nodejs()
    }

    if (desktop) {
      jvm("desktop")
    }

    if (android) androidTarget {
      publishLibraryVariants("release")
      compilerOptions {
        jvmTarget.set(conventionExtension.jvmTarget)
        freeCompilerArgs.addAll(Config.jvmCompilerArgs)
      }
    }

    if (jvm) jvm {
      compilerOptions {
        jvmTarget.set(conventionExtension.jvmTarget)
        freeCompilerArgs.addAll(Config.jvmCompilerArgs)
      }
    }

    // TODO-Improve: Set binary base name etc.
    sequence {
      if (iOS) {
        yield(iosX64())
        yield(iosArm64())
        yield(iosSimulatorArm64())
      }
      if (macOS) {
        yield(macosArm64())
        yield(macosX64())
      }
      if (tvOS) {
        yield(tvosX64())
        yield(tvosArm64())
        yield(tvosSimulatorArm64())
      }
      if (watchOS) {
        yield(watchosX64())
        yield(watchosArm64())
        yield(watchosDeviceArm64())
        yield(watchosSimulatorArm64())
      }
    }.toList().onEach {
      it.binaries.framework {
        baseName = path.substring(1).replace(':', '_')
        val bundleId = path.substring(1)
          .replace(':', '.')
          .replace(Regex("-(.)")) { matchResult ->
            matchResult.groupValues[1].uppercase()
          }
        freeCompilerArgs += listOf("-Xbinary=bundleId=$bundleId")
        isStatic = true
      }
    }

    val libs by versionCatalog
    sourceSets.apply {
      if (jvm) {
        val jvmTest by getting {
          dependencies {
            implementation(libs.requireLib("kotest-junit"))
          }
        }
      }
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
            freeCompilerArgs.addAll(Config.compilerArgs)
            optIn.addAll(Config.optIns)
            progressiveMode.set(true)
          }
        }
      }
    }
  }
}
