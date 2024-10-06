package com.indramahkota.convention.compose

import com.indramahkota.convention.common.annotation.InternalPluginApi
import com.indramahkota.convention.common.constant.PLUGIN_ID_CONVENTION_ANDROID_APP
import com.indramahkota.convention.common.constant.PLUGIN_ID_KOTLIN_COMPOSE_COMPILER
import com.indramahkota.convention.common.internal.applyPlugins
import com.indramahkota.convention.common.internal.requiredPlugin
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry

public class ComposeApplicationPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseComposePlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_KOTLIN_COMPOSE_COMPILER,
      errorMessage = "Compose Compiler Gradle Plugin not found.",
    )
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_CONVENTION_ANDROID_APP,
      errorMessage = "Convention Android Application Gradle Plugin not found.",
    )

    applyPlugins(PLUGIN_ID_CONVENTION_ANDROID_APP, PLUGIN_ID_KOTLIN_COMPOSE_COMPILER)

    configureCommonComposeAndroid()
  }
}
