package convention.common.internal

import convention.common.annotation.InternalPluginApi
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.plugin.use.internal.DefaultPluginId

/**
 * It checks if a plugin with the given ID is present in the registry.
 *
 * @param pluginId The ID of the plugin to check.
 * @return True if the plugin with the given ID is present in the registry, false otherwise.
 */
@InternalPluginApi
public fun PluginRegistry.hasPlugin(pluginId: String): Boolean =
  lookup(DefaultPluginId.unvalidated(pluginId)) != null

/**
 * It checks if a plugin with the given ID is required in the registry.
 *
 * @param pluginId The ID of the plugin to check.
 * @param errorMessage The error message to use if the plugin is not present.
 */
@OptIn(InternalPluginApi::class)
public fun PluginRegistry.requiredPlugin(pluginId: String, errorMessage: String) {
  check(hasPlugin(pluginId)) { errorMessage }
}

/**
 * It applies a list of plugins to the project.
 *
 * @param pluginIds The IDs of the plugins to apply.
 */
public fun Project.applyPlugins(vararg pluginIds: String) {
  pluginIds.forEach { pluginManager.apply(it) }
}
