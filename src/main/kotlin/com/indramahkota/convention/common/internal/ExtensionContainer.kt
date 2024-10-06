package com.indramahkota.convention.common.internal

import org.gradle.api.plugins.ExtensionContainer

/**
 * This function is an extension function for the ExtensionContainer class.
 * It is used to find an extension by its name and cast it to the specified type.
 * The function is marked with the @PublishedApi annotation, which means it is part of the public API
 * but should not be used directly outside of this module.
 * The function is also marked as internal, which means it is only visible within the same module.
 * The function is inline and uses a reified type parameter, which allows for type-safe casting.
 *
 * @param name The name of the extension to find.
 * @return The extension with the specified name, cast to the type T, or null if no such extension exists or the cast is not possible.
 */
@PublishedApi
internal inline fun <reified T : Any> ExtensionContainer.findExtByName(name: String): T? =
  findByName(name) as? T
