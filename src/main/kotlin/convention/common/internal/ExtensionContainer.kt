package convention.common.internal

import org.gradle.api.plugins.ExtensionContainer

/**
 * It is used to find an extension by its name and cast it to the specified type.
 *
 * @param name The name of the extension to find.
 * @return The extension with the specified name, cast to the type T, or null if no such extension exists or the cast is not possible.
 */
@PublishedApi
internal inline fun <reified T : Any> ExtensionContainer.findExtByName(name: String): T? =
  findByName(name) as? T
