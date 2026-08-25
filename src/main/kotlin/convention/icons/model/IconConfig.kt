package convention.icons.model

import kotlinx.serialization.Serializable

private fun sanitizeIconName(iconName: String): String {
  return iconName
    .replace("/", "_")
    .replace("\\", "_")
    .replace(Regex("[^a-zA-Z0-9_-]"), "_")
    .replace(Regex("_+"), "_")
    .trim('_')
    .ifEmpty { "icon" }
}

public interface IconConfig {
  public val libraryId: String
  public fun buildUrl(iconName: String): String
  public fun getCacheKey(iconName: String): String
  public fun getSignature(): String
}

@Serializable
public data class LocalIconConfig(
  val libraryName: String,
  val absolutePath: String,
  val relativePath: String,
) : IconConfig {

  override val libraryId: String = libraryName

  override fun buildUrl(iconName: String): String = absolutePath

  override fun getCacheKey(iconName: String): String {
    val normalized = absolutePath.replace("\\", "/")
    val hash = normalized.lowercase().hashCode().toString(16)
    return "${libraryId}_$hash"
  }

  override fun getSignature(): String = buildSignature(relativePath)

  private fun buildSignature(relativePath: String): String {
    val normalized = relativePath.replace("/", "_").replace("\\", "_").replace("-", "_")
    val cleaned =
      normalized
        .replace(Regex("[^a-zA-Z0-9_]"), "_")
        .replace(Regex("_+"), "_")
        .trim('_')
    if (cleaned.isBlank()) return "Local"
    return cleaned
      .split('_')
      .filter { it.isNotBlank() }
      .joinToString(separator = "") { part -> part.replaceFirstChar { ch -> ch.titlecase() } }
      .ifBlank { "Local" }
  }
}

@Serializable
public data class MaterialSymbolsConfig(
  val weight: SymbolWeight = SymbolWeight.W400,
  val variant: SymbolVariant = SymbolVariant.OUTLINED,
  val fill: SymbolFill = SymbolFill.UNFILLED,
  val grade: Int = 0,
  val opticalSize: Int = 24,
) : IconConfig {

  override val libraryId: String = "material-symbols"

  override fun buildUrl(iconName: String): String {
    val weightValue =
      when {
        (weight == SymbolWeight.REGULAR || weight == SymbolWeight.W400) &&
          fill == SymbolFill.FILLED -> ""

        (weight == SymbolWeight.REGULAR || weight == SymbolWeight.W400) -> "default"
        else -> "wght${weight.value}"
      }

    return "https://fonts.gstatic.com/s/i/short-term/release/materialsymbols${variant.pathName}/$iconName/$weightValue${fill.shortName}/${opticalSize}px.svg"
  }

  override fun getCacheKey(iconName: String): String {
    val safeName = sanitizeIconName(iconName)
    return "${safeName}_${libraryId}_${weight.value}_${variant.pathName}_${fill.name.lowercase()}"
  }

  override fun getSignature(): String = buildString {
    append("W").append(weight.value)
    append(variant.shortName)
    append(fill.shortName)
    if (grade != 0) append("G").append(grade)
  }
}

@Serializable
public data class ExternalIconConfig(
  val libraryName: String,
  val urlTemplate: String,
  val styleParams: Map<String, String> = emptyMap(),
) : IconConfig {
  init {
    validateUrlTemplate(urlTemplate)
    validateLibraryName(libraryName)
  }

  override val libraryId: String = "external-$libraryName"

  override fun buildUrl(iconName: String): String {
    var url = urlTemplate.replace("{name}", iconName)
    styleParams.forEach { (key, value) -> url = url.replace("{$key}", value) }
    return url
  }

  override fun getCacheKey(iconName: String): String {
    val safeName = sanitizeIconName(iconName)
    val paramsString =
      styleParams.entries.sortedBy { it.key }.joinToString("_") { "${it.key}=${it.value}" }
    return "${safeName}_${libraryName}_${paramsString.hashCode()}"
  }

  override fun getSignature(): String {
    return styleParams.values
      .joinToString("") { it.replaceFirstChar { c -> c.titlecase() } }
      .ifEmpty { libraryName.replaceFirstChar { it.titlecase() } }
  }

  public companion object {
    private fun validateUrlTemplate(urlTemplate: String) {
      require(urlTemplate.isNotBlank()) { "URL template cannot be blank" }

      require(urlTemplate.startsWith("https://", ignoreCase = true)) {
        "URL template must start with 'https://' for security. Got: $urlTemplate"
      }

      require(!urlTemplate.contains(" ")) { "URL template contains spaces, which is invalid" }

      val dangerousPatterns =
        listOf("javascript:", "data:", "file:", "ftp:", "<script", "onload=", "onerror=")

      dangerousPatterns.forEach { pattern ->
        require(!urlTemplate.contains(pattern, ignoreCase = true)) {
          "URL template contains potentially dangerous pattern: '$pattern'"
        }
      }
    }

    private fun validateLibraryName(libraryName: String) {
      require(libraryName.isNotBlank()) { "Library name cannot be blank" }

      require(libraryName.matches(Regex("[a-zA-Z0-9_-]+"))) {
        "Library name can only contain alphanumeric characters, hyphens, and underscores. Got: $libraryName"
      }

      require(libraryName.length <= 50) {
        "Library name is too long (max 50 characters). Got: ${libraryName.length}"
      }
    }
  }
}

@Serializable
public enum class SymbolVariant(
  public val shortName: String,
  public val pathName: String
) {
  OUTLINED("Outlined", "outlined"),
  ROUNDED("Rounded", "rounded"),
  SHARP("Sharp", "sharp"),
}

@Serializable
public enum class SymbolFill(
  public val shortName: String
) {
  UNFILLED(""),
  FILLED("fill1"),
}

@Serializable
public enum class SymbolWeight(
  public val value: Int
) {
  W100(100),
  W200(200),
  W300(300),
  W400(400),
  W500(500),
  W600(600),
  W700(700);

  public companion object {
    public val THIN: SymbolWeight = W100
    public val EXTRA_LIGHT: SymbolWeight = W200
    public val LIGHT: SymbolWeight = W300
    public val REGULAR: SymbolWeight = W400
    public val MEDIUM: SymbolWeight = W500
    public val SEMI_BOLD: SymbolWeight = W600
    public val BOLD: SymbolWeight = W700

    public fun fromValue(value: Int): SymbolWeight {
      return entries.find { it.value == value }
        ?: throw IllegalArgumentException(
          "Unsupported weight: $value. Supported weights: ${entries.map { it.value }}"
        )
    }
  }

  override fun toString(): String = value.toString()
}

public object MaterialSymbolsPresets {
  public val W400: MaterialSymbolsConfig = MaterialSymbolsConfig(weight = SymbolWeight.W400)
  public val W500: MaterialSymbolsConfig = MaterialSymbolsConfig(weight = SymbolWeight.W500)
  public val W700: MaterialSymbolsConfig = MaterialSymbolsConfig(weight = SymbolWeight.W700)
  public val W400Filled: MaterialSymbolsConfig =
    MaterialSymbolsConfig(weight = SymbolWeight.W400, fill = SymbolFill.FILLED)
  public val W500Filled: MaterialSymbolsConfig =
    MaterialSymbolsConfig(weight = SymbolWeight.W500, fill = SymbolFill.FILLED)
  public val W400Rounded: MaterialSymbolsConfig =
    MaterialSymbolsConfig(weight = SymbolWeight.W400, variant = SymbolVariant.ROUNDED)
  public val W400Sharp: MaterialSymbolsConfig =
    MaterialSymbolsConfig(weight = SymbolWeight.W400, variant = SymbolVariant.SHARP)
  public val Regular: MaterialSymbolsConfig = W400
  public val Medium: MaterialSymbolsConfig = W500
  public val Bold: MaterialSymbolsConfig = W700
  public val RegularFilled: MaterialSymbolsConfig = W400Filled
  public val MediumFilled: MaterialSymbolsConfig = W500Filled
  public val Rounded: MaterialSymbolsConfig = W400Rounded
  public val Sharp: MaterialSymbolsConfig = W400Sharp
}
