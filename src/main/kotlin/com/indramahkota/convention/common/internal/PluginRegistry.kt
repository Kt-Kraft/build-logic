package com.indramahkota.convention.common.internal

import com.indramahkota.convention.common.annotation.InternalPluginApi
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.plugin.use.internal.DefaultPluginId

/**
 * This function is an extension function for the PluginRegistry class.
 * It checks if a plugin with the given ID is present in the registry.
 *
 * The function is marked with the @InternalPluginApi annotation, which means it is part of the public API
 * but is internal to the plugin and not intended to be used in client code.
 * The usage of this API in client code can lead to unpredictable results and is generally discouraged.
 *
 * @param pluginId The ID of the plugin to check.
 * @return True if the plugin with the given ID is present in the registry, false otherwise.
 */
@InternalPluginApi
public fun PluginRegistry.hasPlugin(pluginId: String): Boolean =
  lookup(DefaultPluginId.unvalidated(pluginId)) != null

/**
 * This function is an extension function for the PluginRegistry class.
 * It checks if a plugin with the given ID is required in the registry.
 *
 * The function is marked with the @OptIn annotation with the InternalPluginApi class, which means it opts into using APIs marked with the @InternalPluginApi annotation.
 * This function will throw an exception if the plugin with the given ID is not present in the registry.
 * The exception message will be the provided error message.
 *
 * @param pluginId The ID of the plugin to check.
 * @param errorMessage The error message to use if the plugin is not present.
 */
@OptIn(InternalPluginApi::class)
public fun PluginRegistry.requiredPlugin(pluginId: String, errorMessage: String) {
  check(hasPlugin(pluginId)) { errorMessage }
}

/**
 * This function is an extension function for the Project class.
 * It applies a list of plugins to the project.
 *
 * The function takes a variable number of arguments (vararg), which means it can take any number of arguments of the specified type.
 * The function then iterates over each plugin ID and applies the corresponding plugin to the project.
 *
 * @param pluginIds The IDs of the plugins to apply.
 */
public fun Project.applyPlugins(vararg pluginIds: String) {
  pluginIds.forEach { pluginManager.apply(it) }
}
