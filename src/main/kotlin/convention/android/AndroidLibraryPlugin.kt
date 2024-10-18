package convention.android

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import convention.android.internal.androidComponents
import convention.android.internal.projectProguardFiles
import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_ANDROID_LIBRARY
import convention.common.internal.applyPlugins
import convention.common.internal.requiredPlugin
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.plugin.AbstractKotlinAndroidPluginWrapper
import javax.inject.Inject

/**
 * This class represents the Android Library Plugin.
 * It extends the BaseAndroidPlugin class and is used to configure the project.
 *
 * @property pluginRegistry The registry of plugins used in the project. This is injected in the constructor.
 * @constructor Creates an instance of the AndroidLibraryPlugin class.
 */
public class AndroidLibraryPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseAndroidPlugin() {

  /**
   * This function is used to configure the project.
   * It checks for the required plugins and applies them to the project.
   * It also configures Kotlin, common Android settings, library settings, and finalizes the library Android settings.
   */
  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_ANDROID_LIBRARY,
      errorMessage = "Android Library Gradle Plugin not found.",
    )

    applyPlugins(PLUGIN_ID_ANDROID_LIBRARY)

    configureCommonAndroid()
    configureLibrary()
    configureKotlin()
    finalizeLibraryAndroid()
  }
}

private fun Project.configureLibrary() =
  extensions.configure<LibraryExtension> {
    defaultConfig {
      // Add all files from 'proguard' dir
      consumerProguardFiles.addAll(projectProguardFiles())
    }
  }

private fun Project.configureKotlin() {
  plugins.withType<AbstractKotlinAndroidPluginWrapper> {
    configure<KotlinAndroidProjectExtension> {
      explicitApi()
    }
  }
}

// @see: https://developer.android.com/reference/tools/gradle-api/7.0/com/android/build/api/extension/LibraryAndroidComponentsExtension
/**
 * Disable unnecessary Android instrumented tests for the project if there is no `androidTest` folder.
 * Otherwise, these projects would be compiled, packaged, installed and ran only to end-up with the following message:
 *
 * > Starting 0 tests on AVD
 *
 * Note: this could be improved by checking other potential sourceSets based on buildTypes and flavors.
 */
private fun Project.finalizeLibraryAndroid() =
  androidComponents<LibraryAndroidComponentsExtension> {
    beforeVariants {
      it.androidTest.enable = it.androidTest.enable
        && project.projectDir.resolve("src/androidTest").exists()
    }
  }
