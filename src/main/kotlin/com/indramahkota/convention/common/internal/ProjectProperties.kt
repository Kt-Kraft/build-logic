package com.indramahkota.convention.common.internal

import com.indramahkota.convention.common.annotation.InternalPluginApi
import org.gradle.api.Project

/**
 * This function is an extension function for the Project class.
 * It is used to find a Gradle property by its name and return its value as a String.
 * The function is marked with the @InternalPluginApi annotation, which means it is part of the public API
 * but is internal to the plugin and not intended to be used in client code.
 * The usage of this API in client code can lead to unpredictable results and is generally discouraged.
 *
 * @param propertyName The name of the Gradle property to find.
 * @return The value of the Gradle property as a String, or null if the property is not found.
 */
@InternalPluginApi
public fun Project.findStringProperty(propertyName: String): String? {
  return providers.gradleProperty(propertyName).orNull
}

/**
 * This function is an extension function for the Project class.
 * It is used to find a Gradle property by its name and return its value as a Boolean.
 * The function is marked with the @InternalPluginApi annotation, which means it is part of the public API
 * but is internal to the plugin and not intended to be used in client code.
 * The usage of this API in client code can lead to unpredictable results and is generally discouraged.
 *
 * The function uses the findStringProperty function to get the property value as a String, and then converts it to a Boolean.
 * If the property is not found or cannot be converted to a Boolean, the function returns null.
 *
 * @param propertyName The name of the Gradle property to find.
 * @return The value of the Gradle property as a Boolean, or null if the property is not found or cannot be converted to a Boolean.
 */
@InternalPluginApi
public fun Project.findBooleanProperty(propertyName: String): Boolean? {
  return findStringProperty(propertyName)?.toBoolean()
}
