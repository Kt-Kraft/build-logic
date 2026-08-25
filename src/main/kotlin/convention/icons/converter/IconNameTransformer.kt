package convention.icons.converter

import java.io.Serializable
import net.pearx.kasechange.toCamelCase
import net.pearx.kasechange.toKebabCase
import net.pearx.kasechange.toPascalCase
import net.pearx.kasechange.toScreamingSnakeCase
import net.pearx.kasechange.toSnakeCase

public abstract class IconNameTransformer : Serializable {

  public abstract fun transform(fileName: String): String

  public open fun getSignature(): String = this::class.java.name
}

public enum class NamingConvention {
  PASCAL_CASE,
  CAMEL_CASE,
  SNAKE_CASE,
  SCREAMING_SNAKE,
  KEBAB_CASE,
  LOWER_CASE,
  UPPER_CASE,
}

public class ConventionNameTransformer(
  private val convention: NamingConvention = NamingConvention.PASCAL_CASE,
  private val suffix: String = "",
  private val prefix: String = "",
  private val removePrefix: String = "",
  private val removeSuffix: String = "",
) : IconNameTransformer() {

  override fun transform(fileName: String): String {
    val cleaned =
      fileName
        .removeSuffix(".svg")
        .let { if (removePrefix.isNotEmpty()) it.removePrefix(removePrefix) else it }
        .let { if (removeSuffix.isNotEmpty()) it.removeSuffix(removeSuffix) else it }

    val converted =
      when (convention) {
        NamingConvention.PASCAL_CASE -> cleaned.toPascalCase()
        NamingConvention.CAMEL_CASE -> cleaned.toCamelCase()
        NamingConvention.SNAKE_CASE -> cleaned.toSnakeCase()
        NamingConvention.SCREAMING_SNAKE -> cleaned.toScreamingSnakeCase()
        NamingConvention.KEBAB_CASE -> cleaned.toKebabCase()
        NamingConvention.LOWER_CASE -> cleaned.lowercase().replace(Regex("[^a-z0-9]"), "")
        NamingConvention.UPPER_CASE -> cleaned.uppercase().replace(Regex("[^A-Z0-9]"), "")
      }

    return "$prefix$converted$suffix"
  }

  public fun fromConvention(
    convention: NamingConvention,
    suffix: String = "",
    prefix: String = "",
    removePrefix: String = "",
    removeSuffix: String = "",
  ): IconNameTransformer =
    ConventionNameTransformer(
      convention = convention,
      suffix = suffix,
      prefix = prefix,
      removePrefix = removePrefix,
      removeSuffix = removeSuffix,
    )
}
