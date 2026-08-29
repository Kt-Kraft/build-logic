package convention.android

import convention.android.internal.CommonExtension
import convention.android.internal.android
import convention.common.BaseConventionPlugin
import convention.common.ConventionOptionsExtension
import convention.common.annotation.InternalPluginApi
import convention.common.utils.addDistinctCompilerArgs
import convention.common.utils.addDistinctOptIns
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.plugin.AbstractKotlinAndroidPluginWrapper

public abstract class BaseAndroidPlugin : BaseConventionPlugin() {

  protected val androidOptions: AndroidOptionsExtension
    get() = conventionOptions.extensions.androidOptions

  @InternalPluginApi
  protected fun Project.configureCommonAndroid() {
    configureKotlinAndroid(conventionOptions)
    configureCommonAndroid(androidOptions, conventionOptions)
  }
}

private fun Project.configureKotlinAndroid(
  conventionOptions: ConventionOptionsExtension,
) {
  plugins.withType<AbstractKotlinAndroidPluginWrapper> {
    configure<KotlinAndroidProjectExtension> {
      explicitApi()
      compilerOptions {
        addDistinctCompilerArgs(conventionOptions.freeCompilerArgs.get())
        addDistinctOptIns(conventionOptions.optIns.get())
        progressiveMode.set(true)
        jvmTarget.set(conventionOptions.jvmTarget)
      }
      jvmToolchain {
        languageVersion.set(conventionOptions.jvmToolchainLanguageVersion)
      }
    }
  }
}

private fun Project.configureCommonAndroid(
  androidOptions: AndroidOptionsExtension,
  conventionOptions: ConventionOptionsExtension,
) = android<CommonExtension> {
  // Ensure that compileSdk is never lower than targetSdk.
  // - If compileSdk <= targetSdk, use targetSdk instead (safe upper bound).
  // - Otherwise, use the declared compileSdk.
  // This avoids build errors or warnings caused by setting compileSdk
  // lower than the app’s targetSdk (which Android Gradle Plugin disallows).
  compileSdk {
    version = release(
      maxOf(
        androidOptions.compileSdk.get(),
        androidOptions.targetSdk.get()
      )
    )
  }

  // Sets the minimum Android SDK version supported by the app.
  defaultConfig.minSdk = androidOptions.minSdk.get()

  // Configure Java compilation compatibility.
  // Both sourceCompatibility and targetCompatibility are set to the
  // value from javaVersion (default: Java 17).
  //
  // - sourceCompatibility → Which Java language features can be used.
  // - targetCompatibility → The version of JVM bytecode generated.
  //
  // Keeping them equal ensures consistent compilation and avoids
  // mismatches with the Kotlin jvmTarget setting.
  compileOptions.apply {
    sourceCompatibility = conventionOptions.javaVersion.get()
    targetCompatibility = conventionOptions.javaVersion.get()
  }

  // Due to https://github.com/Kotlin/kotlinx.coroutines/issues/2023
  packaging.resources {
    // The set of excluded patterns.
    // Java resources matching any of these patterns do not get packaged in the APK.
    excludes += listOf(
      // Licenses & notices
      "META-INF/LICENSE*",
      "META-INF/NOTICE*",
      "META-INF/AL2.0",
      "META-INF/LGPL2.1",

      // Metadata & duplicates
      "META-INF/*.kotlin_module",
      "META-INF/*.version",
      "META-INF/INDEX.LIST",
      "META-INF/DEPENDENCIES",
      "META-INF/gradle/incremental.annotation.processors",

      // Annotation processor outputs
      "META-INF/services/*",

      // Other unnecessary files
      "META-INF/native-image/**",
      "META-INF/proguard/*"
    )
  }
}
