package convention.common

import kotlin.reflect.KClass
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.create

public interface WithDefaults<T> {

  public fun setDefaults(defaults: T)
}

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
