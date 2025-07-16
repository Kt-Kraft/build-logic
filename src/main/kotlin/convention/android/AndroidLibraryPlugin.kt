package convention.android

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import convention.android.internal.androidComponents
import convention.android.internal.projectProguardFiles
import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_ANDROID_LIBRARY
import convention.common.internal.applyPlugins
import convention.common.internal.requiredPlugin
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.kotlin.dsl.configure

public class AndroidLibraryPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseAndroidPlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_ANDROID_LIBRARY,
      errorMessage = "Android Library Gradle Plugin not found.",
    )
    applyPlugins(PLUGIN_ID_ANDROID_LIBRARY)

    configureKotlinAndroid()
    configureCommonAndroid()
    configureLibraryAndroid()
    finalizeLibraryAndroid()
  }
}

private fun Project.configureLibraryAndroid() =
  extensions.configure<LibraryExtension> {
    defaultConfig {
      // Add all files from 'proguard' dir
      consumerProguardFiles.addAll(projectProguardFiles())
    }
  }

// @see: https://developer.android.com/reference/tools/gradle-api/7.0/com/android/build/api/extension/LibraryAndroidComponentsExtension
private fun Project.finalizeLibraryAndroid() =
  androidComponents<LibraryAndroidComponentsExtension> {
    beforeVariants {
      it.androidTest.enable = it.androidTest.enable
        && project.projectDir.resolve("src/androidTest").exists()
    }
  }
