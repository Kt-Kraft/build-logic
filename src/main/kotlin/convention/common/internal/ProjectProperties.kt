package convention.common.internal

import convention.common.annotation.InternalPluginApi
import org.gradle.api.Project

/**
 * It is used to find a Gradle property by its name and return its value as a String.
 *
 * @param propertyName The name of the Gradle property to find.
 * @return The value of the Gradle property as a String, or null if the property is not found.
 */
@InternalPluginApi
public fun Project.findStringProperty(propertyName: String): String? {
  return providers.gradleProperty(propertyName).orNull
}

/**
 * It is used to find a Gradle property by its name and return its value as a Boolean.
 *
 * @param propertyName The name of the Gradle property to find.
 * @return The value of the Gradle property as a Boolean, or null if the property is not found or cannot be converted to a Boolean.
 */
@InternalPluginApi
public fun Project.findBooleanProperty(propertyName: String): Boolean? {
  return findStringProperty(propertyName)?.toBoolean()
}
