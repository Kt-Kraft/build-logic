package com.indramahkota.convention.common

import kotlin.reflect.KClass
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.create

/**
 * This is a public interface that represents an entity that can have default values set.
 * It is a generic interface with a type parameter T.
 *
 * @param T The type of the default values.
 */
public interface WithDefaults<T> {

  /**
   * Sets the default values for the entity.
   *
   * @param defaults The default values to set.
   */
  public fun setDefaults(defaults: T)
}

/**
 * This is an extension function for the ExtensionContainer class.
 * It is used to create an extension with default values.
 *
 * The function is generic and takes a type parameter T that must be a subtype of WithDefaults<T>.
 * It takes three parameters: the name of the extension, the default values for the extension, and the public type of the extension.
 *
 * The function first checks if the public type is null. If it is, it creates an extension with the specified name.
 * If the public type is not null, it creates an extension with the specified public type, name, and the type parameter T.
 * The created extension is then cast to the type parameter T.
 *
 * After the extension is created, the function checks if the default values are not null.
 * If they are not, it sets the default values for the extension.
 *
 * The function finally returns the created extension.
 *
 * @param name The name of the extension.
 * @param defaults The default values for the extension. If null, no default values are set.
 * @param publicType The public type of the extension. If null, the type is inferred from the type parameter T.
 * @return The created extension.
 */
@PublishedApi
internal inline fun <reified T : WithDefaults<T>> ExtensionContainer.createWithDefaults(
  name: String,
  defaults: T?,
  publicType: KClass<in T>? = null,
): T {
  val extension = if (publicType == null) {
    create(name)
  } else {
    create(publicType, name, T::class) as T
  }
  return extension.apply { if (defaults != null) setDefaults(defaults) }
}
