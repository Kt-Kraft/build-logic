package convention.android

import com.android.build.api.dsl.CommonExtension
import convention.android.internal.android
import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.plugin.AbstractKotlinAndroidPluginWrapper

/**
 * This abstract class represents the Base Android Plugin.
 * It extends the BaseConventionPlugin class and is used to configure the project.
 *
 * @property androidOptionsExtension The Android options extension used in the project. It is a protected property and is accessed via a getter.
 */
public abstract class BaseAndroidPlugin : BaseConventionPlugin() {

  /**
   * This is a getter for the androidOptionsExtension property.
   * It retrieves the AndroidOptionsExtension instance from the conventionExtension's extensions.
   *
   * @return The AndroidOptionsExtension instance.
   */
  protected val androidOptionsExtension: AndroidOptionsExtension
    get() = conventionExtension.extensions.androidOptions

  /**
   * This function is used to configure the common Android settings for the project.
   * It configures the common Kotlin settings and the common settings for the Android project.
   *
   * @receiver The Project instance on which the function is invoked.
   */
  @InternalPluginApi
  protected fun Project.configureCommonAndroid() {
    configureCommon(androidOptionsExtension, conventionExtension.jvmTarget)
    configureKotlin()
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

private fun Project.configureKotlin() {
  plugins.withType<AbstractKotlinAndroidPluginWrapper> {
    configure<KotlinAndroidProjectExtension> {
      compilerOptions {
        freeCompilerArgs.addAll(
          listOf(
            "-opt-in=kotlin.Experimental",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
          ),
        )
      }
    }
  }
}
