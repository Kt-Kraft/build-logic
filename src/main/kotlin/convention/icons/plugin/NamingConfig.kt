package convention.icons.plugin

import convention.icons.converter.IconNameTransformer
import convention.icons.converter.NamingConvention
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

public abstract class NamingConfig @Inject constructor(private val objects: ObjectFactory) {
  public abstract val namingConvention: Property<NamingConvention>

  public abstract val suffix: Property<String>

  public abstract val prefix: Property<String>

  public abstract val removePrefix: Property<String>

  public abstract val removeSuffix: Property<String>

  public abstract val transformer: Property<IconNameTransformer>

  init {
    namingConvention.convention(NamingConvention.PASCAL_CASE)
    suffix.convention("")
    prefix.convention("")
    removePrefix.convention("")
    removeSuffix.convention("")
  }

  public fun pascalCase(suffix: String = "", prefix: String = "") {
    namingConvention.set(NamingConvention.PASCAL_CASE)
    this.suffix.set(suffix)
    this.prefix.set(prefix)
  }

  public fun camelCase(suffix: String = "", prefix: String = "") {
    namingConvention.set(NamingConvention.CAMEL_CASE)
    this.suffix.set(suffix)
    this.prefix.set(prefix)
  }

  public fun snakeCase(uppercase: Boolean = false) {
    namingConvention.set(
      if (uppercase) NamingConvention.SCREAMING_SNAKE else NamingConvention.SNAKE_CASE
    )
  }

  public fun kebabCase() {
    namingConvention.set(NamingConvention.KEBAB_CASE)
  }

  public fun lowerCase() {
    namingConvention.set(NamingConvention.LOWER_CASE)
  }

  public fun upperCase() {
    namingConvention.set(NamingConvention.UPPER_CASE)
  }

  public fun customTransformer(transformer: IconNameTransformer) {
    this.transformer.set(transformer)
  }

  internal fun snapshotSignature(): String {
    val builder = StringBuilder()
    builder.append("convention=")
    builder.append(namingConvention.orNull)
    builder.append("|suffix=")
    builder.append(suffix.orNull)
    builder.append("|prefix=")
    builder.append(prefix.orNull)
    builder.append("|removePrefix=")
    builder.append(removePrefix.orNull)
    builder.append("|removeSuffix=")
    builder.append(removeSuffix.orNull)
    val customTransformer = transformer.orNull
    builder.append("|transformer=")
    if (customTransformer != null) {
      builder.append(customTransformer.getSignature())
    } else {
      builder.append("null")
    }
    return builder.toString()
  }
}
