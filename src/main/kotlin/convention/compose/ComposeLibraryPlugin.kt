package convention.compose

import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_CONVENTION_ANDROID_LIB
import convention.common.constant.PLUGIN_ID_KOTLIN_COMPOSE_COMPILER
import convention.common.internal.applyPlugins
import convention.common.internal.requiredPlugin
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry

public class ComposeLibraryPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseComposePlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_KOTLIN_COMPOSE_COMPILER,
      errorMessage = "Compose Compiler Gradle Plugin not found.",
    )
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_CONVENTION_ANDROID_LIB,
      errorMessage = "Convention Android Library Gradle Plugin not found.",
    )
    applyPlugins(PLUGIN_ID_CONVENTION_ANDROID_LIB, PLUGIN_ID_KOTLIN_COMPOSE_COMPILER)

    configureCommonComposeAndroid()
  }
}
