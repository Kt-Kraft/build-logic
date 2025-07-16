package convention.android

import com.android.build.api.dsl.CommonExtension
import convention.android.internal.android
import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import convention.common.utils.Config
import convention.common.utils.addDistinctCompilerArgs
import convention.common.utils.addDistinctOptIns
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.plugin.AbstractKotlinAndroidPluginWrapper

public abstract class BaseAndroidPlugin : BaseConventionPlugin() {

  protected val androidOptionsExtension: AndroidOptionsExtension
    get() = conventionExtension.extensions.androidOptions

  @InternalPluginApi
  protected fun Project.configureCommonAndroid() {
    configureCommon(androidOptionsExtension, conventionExtension.javaVersion)
  }
}

private fun Project.configureCommon(
  androidOptions: AndroidOptionsExtension,
  jvmTarget: Provider<JavaVersion>,
) = android<CommonExtension<*, *, *, *, *, *>> {
  val compileSdkCondition = androidOptions.compileSdk.get() <= androidOptions.targetSdk.get()
  compileSdk = if (compileSdkCondition) androidOptions.targetSdk.get() else androidOptions.compileSdk.get()

  defaultConfig {
    minSdk = androidOptions.minSdk.get()
  }

  compileOptions {
    sourceCompatibility = jvmTarget.get()
    targetCompatibility = jvmTarget.get()
  }
}

internal fun Project.configureKotlinAndroid() {
  plugins.withType<AbstractKotlinAndroidPluginWrapper> {
    configure<KotlinAndroidProjectExtension> {
      explicitApi()
      compilerOptions {
        addDistinctCompilerArgs(Config.compilerArgs)
        addDistinctOptIns(Config.optIns)
        progressiveMode.set(true)
      }
    }
  }
}
