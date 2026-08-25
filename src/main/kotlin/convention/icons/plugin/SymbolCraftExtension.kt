package convention.icons.plugin

import convention.icons.model.ExternalIconConfig
import convention.icons.model.IconConfig
import convention.icons.model.LocalIconConfig
import convention.icons.model.MaterialSymbolsConfig
import convention.icons.model.SymbolFill
import convention.icons.model.SymbolVariant
import convention.icons.model.SymbolWeight
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

public abstract class SymbolCraftExtension {
  @get:Inject
  protected abstract val objects: ObjectFactory
  public abstract val cacheEnabled: Property<Boolean>
  public abstract val cacheDirectory: Property<String>
  public abstract val outputDirectory: Property<String>
  public abstract val packageName: Property<String>
  public abstract val generatePreview: Property<Boolean>
  public abstract val maxRetries: Property<Int>
  public abstract val retryDelayMs: Property<Long>
  public abstract val projectDirectory: Property<String>

  internal val namingConfig: NamingConfig by lazy {
    objects.newInstance(NamingConfig::class.java)
  }

  private val iconsConfig = mutableMapOf<String, MutableList<IconConfig>>()

  init {
    cacheEnabled.convention(true)
    cacheDirectory.convention("symbolcraft-cache")
    outputDirectory.convention("src/main/kotlin")
    packageName.convention("core.designSystem.symbolcraft.symbols")
    generatePreview.convention(false)
    maxRetries.convention(3)
    retryDelayMs.convention(1000L)
    projectDirectory.convention("")
  }

  public fun naming(action: Action<NamingConfig>) {
    action.execute(namingConfig)
  }

  internal fun namingConfigSignature(): String = namingConfig.snapshotSignature()

  public fun iconConfig(name: String, config: IconConfig) {
    iconsConfig.getOrPut(name) { mutableListOf() }.add(config)
  }

  public fun iconConfigs(vararg names: String, configFactory: (String) -> IconConfig) {
    names.filter { it.isNotEmpty() }.forEach { name -> iconConfig(name, configFactory(name)) }
  }

  public fun materialSymbol(name: String, configure: MaterialSymbolsBuilder.() -> Unit) {
    val builder = MaterialSymbolsBuilder()
    builder.configure()
    builder.configs.forEach { config -> iconConfig(name, config) }
  }

  public fun materialSymbols(vararg names: String, configure: MaterialSymbolsBuilder.() -> Unit) {
    names.filter { it.isNotEmpty() }.forEach { name -> materialSymbol(name, configure) }
  }

  public fun externalIcon(name: String, libraryName: String, configure: ExternalIconBuilder.() -> Unit) {
    val builder = ExternalIconBuilder(libraryName)
    builder.configure()
    val configs = builder.build()
    configs.forEach { config -> iconConfig(name, config) }
  }

  public fun externalIcons(
    vararg names: String,
    libraryName: String,
    configure: ExternalIconBuilder.() -> Unit,
  ) {
    names.filter { it.isNotEmpty() }.forEach { name -> externalIcon(name, libraryName, configure) }
  }

  public fun localIcons(libraryName: String = "local", configure: LocalIconsBuilder.() -> Unit) {
    val projectDir =
      projectDirectory.orNull
        ?: throw IllegalStateException(
          "Project directory is not set on SymbolCraftExtension"
        )

    validateLocalLibraryName(libraryName)

    val builder = LocalIconsBuilder(projectDir)
    builder.configure()
    val localConfigs = builder.build(libraryName)
    localConfigs.forEach { (iconName, config) -> iconConfig(iconName, config) }
  }

  private fun validateLocalLibraryName(libraryName: String) {
    require(libraryName.matches(Regex("[a-zA-Z0-9_-]+"))) {
      "Library name for local icons can only contain alphanumeric characters, hyphens, and underscores. Got: $libraryName"
    }
  }

  public fun getIconsConfig(): Map<String, List<IconConfig>> = iconsConfig.toMap()

  public fun getConfigHash(): String {
    val configString = buildString {
      append("version:2.0|")

      append("icons:")
      iconsConfig.toSortedMap().forEach { (name, configs) ->
        append("$name-[")
        configs
          .sortedBy { "${it.libraryId}-${it.getSignature()}" }
          .forEach { config -> append("${config.libraryId}:${config.getSignature()},") }
        append("]")
      }
      append("|package:").append(packageName.orNull)
      append("|outputDir:").append(outputDirectory.orNull)
      append("|preview:").append(generatePreview.orNull)
      append("|namingConfig:").append(namingConfig.snapshotSignature())
    }
    return configString.hashCode().toString()
  }
}

public class MaterialSymbolsBuilder {
  public val configs: MutableList<MaterialSymbolsConfig> = mutableListOf<MaterialSymbolsConfig>()

  public fun style(
    weight: SymbolWeight = SymbolWeight.W400,
    variant: SymbolVariant = SymbolVariant.OUTLINED,
    fill: SymbolFill = SymbolFill.UNFILLED,
    grade: Int = 0,
    opticalSize: Int = 24,
  ) {
    configs.add(MaterialSymbolsConfig(weight, variant, fill, grade, opticalSize))
  }

  public fun style(
    weight: Int,
    variant: SymbolVariant = SymbolVariant.OUTLINED,
    fill: SymbolFill = SymbolFill.UNFILLED,
    grade: Int = 0,
    opticalSize: Int = 24,
  ) {
    val symbolWeight = SymbolWeight.fromValue(weight)
    configs.add(MaterialSymbolsConfig(symbolWeight, variant, fill, grade, opticalSize))
  }

  public fun weights(
    vararg weights: SymbolWeight,
    variant: SymbolVariant = SymbolVariant.OUTLINED,
    fill: SymbolFill = SymbolFill.UNFILLED,
  ) {
    weights.forEach { weight -> style(weight = weight, variant = variant, fill = fill) }
  }

  public fun weights(
    vararg weights: Int,
    variant: SymbolVariant = SymbolVariant.OUTLINED,
    fill: SymbolFill = SymbolFill.UNFILLED,
  ) {
    weights.forEach { weight -> style(weight = weight, variant = variant, fill = fill) }
  }

  public fun standardWeights(
    variant: SymbolVariant = SymbolVariant.OUTLINED,
    fill: SymbolFill = SymbolFill.UNFILLED,
  ) {
    weights(
      SymbolWeight.W400,
      SymbolWeight.W500,
      SymbolWeight.W700,
      variant = variant,
      fill = fill,
    )
  }

  public fun allVariants(
    weight: SymbolWeight = SymbolWeight.W400,
    fill: SymbolFill = SymbolFill.UNFILLED,
  ) {
    SymbolVariant.entries.forEach { variant ->
      style(weight = weight, variant = variant, fill = fill)
    }
  }

  public fun allVariants(weight: Int, fill: SymbolFill = SymbolFill.UNFILLED) {
    SymbolVariant.entries.forEach { variant ->
      style(weight = weight, variant = variant, fill = fill)
    }
  }

  public fun bothFills(
    weight: SymbolWeight = SymbolWeight.W400,
    variant: SymbolVariant = SymbolVariant.OUTLINED,
  ) {
    style(weight = weight, variant = variant, fill = SymbolFill.UNFILLED)
    style(weight = weight, variant = variant, fill = SymbolFill.FILLED)
  }

  public fun bothFills(weight: Int, variant: SymbolVariant = SymbolVariant.OUTLINED) {
    style(weight = weight, variant = variant, fill = SymbolFill.UNFILLED)
    style(weight = weight, variant = variant, fill = SymbolFill.FILLED)
  }
}

public class ExternalIconBuilder(private val libraryName: String) {
  public var urlTemplate: String = ""
  private val singleValueParams = mutableMapOf<String, String>()
  private val multiValueParams = mutableMapOf<String, List<String>>()

  public fun styleParam(key: String, value: String) {
    singleValueParams[key] = value
  }

  public fun styleParam(key: String, configure: StyleParamBuilder.() -> Unit) {
    val builder = StyleParamBuilder()
    builder.configure()
    multiValueParams[key] = builder.valuesList
  }

  public fun build(): List<ExternalIconConfig> {
    require(urlTemplate.isNotBlank()) { "urlTemplate must be specified for external icon" }

    if (multiValueParams.isEmpty()) {
      return listOf(ExternalIconConfig(libraryName, urlTemplate, singleValueParams.toMap()))
    }

    return generateCartesianProduct().map { paramCombination ->
      ExternalIconConfig(libraryName, urlTemplate, paramCombination)
    }
  }

  private fun generateCartesianProduct(): List<Map<String, String>> {
    val allParams = mutableMapOf<String, List<String>>()
    singleValueParams.forEach { (key, value) -> allParams[key] = listOf(value) }
    allParams.putAll(multiValueParams)
    return cartesianProduct(allParams)
  }

  private fun cartesianProduct(params: Map<String, List<String>>): List<Map<String, String>> {
    if (params.isEmpty()) return listOf(emptyMap())
    return params.entries.fold(listOf(emptyMap())) { acc, (key, values) ->
      acc.flatMap { map -> values.map { value -> map + (key to value) } }
    }
  }
}

public class LocalIconsBuilder internal constructor(private val projectDir: String) {
  public var directory: String? = null

  private val includePatterns = mutableListOf<String>()
  private val excludePatterns = mutableListOf<String>()

  public fun include(vararg patterns: String) {
    patterns
      .filter { it.isNotBlank() }
      .forEach { pattern ->
        includePatterns.add(pattern)
        if (pattern.contains("**/")) {
          val directMatch = pattern.replace("**/", "")
          if (directMatch != pattern) {
            includePatterns.add(directMatch)
          }
        }
      }
  }

  public fun exclude(vararg patterns: String) {
    excludePatterns.addAll(patterns.filter { it.isNotBlank() })
  }

  internal fun build(libraryName: String): Map<String, LocalIconConfig> {
    val dirValue =
      directory?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException("directory must be specified for localIcons")

    val baseDir = resolveAgainstProject(dirValue).canonicalFile

    require(baseDir.exists()) {
      "Local icons directory does not exist: ${baseDir.absolutePath}"
    }
    require(baseDir.isDirectory) {
      "Local icons path must be a directory: ${baseDir.absolutePath}"
    }

    val includes =
      if (includePatterns.isEmpty()) listOf("**/*.svg", "*.svg") else includePatterns
    val includeMatchers = includes.map { compileGlob(it) }
    val excludeMatchers = excludePatterns.map { compileGlob(it) }

    val iconMap = linkedMapOf<String, LocalIconConfig>()

    baseDir
      .walkTopDown()
      .filter { it.isFile && it.extension.equals("svg", ignoreCase = true) }
      .forEach { file ->
        val relativePath = baseDir.toPath().relativize(file.toPath())
        if (!matches(relativePath, includeMatchers)) return@forEach
        if (matches(relativePath, excludeMatchers)) return@forEach

        val relativeNormalized = relativePath.toString().replace(File.separatorChar, '/')
        val iconNameBase = buildIconName(relativeNormalized)
        val iconName = ensureUniqueName(iconNameBase, iconMap.keys)

        val relativeWithoutExt = stripSvgExtension(relativeNormalized)

        iconMap[iconName] =
          LocalIconConfig(
            libraryName = libraryName,
            absolutePath = file.absolutePath,
            relativePath = relativeWithoutExt,
          )
      }

    if (iconMap.isEmpty()) {
      throw IllegalStateException(
        "No SVG icons found in ${baseDir.absolutePath} for includes $includes and excludes $excludePatterns"
      )
    }

    return iconMap
  }

  private fun compileGlob(pattern: String): PathMatcher {
    val normalized = pattern.trim().ifBlank { "**/*.svg" }
    val systemPattern = normalized.replace("/", File.separator)
    return FileSystems.getDefault().getPathMatcher("glob:$systemPattern")
  }

  private fun matches(relativePath: Path, matchers: List<PathMatcher>): Boolean {
    if (matchers.isEmpty()) return false
    return matchers.any { it.matches(relativePath) }
  }

  private fun buildIconName(relativePath: String): String {
    val withoutExt = stripSvgExtension(relativePath)
    val sanitized = sanitizeLocalName(withoutExt)
    return sanitized.ifBlank { "icon" }
  }

  private fun ensureUniqueName(baseName: String, existing: Set<String>): String {
    if (baseName !in existing) return baseName

    var index = 2
    var candidate: String
    do {
      candidate = "${baseName}_${index}"
      index++
    } while (candidate in existing)
    return candidate
  }

  private fun resolveAgainstProject(path: String): File {
    val file = File(path)
    return if (file.isAbsolute) file else File(projectDir, path)
  }

  private fun stripSvgExtension(path: String): String {
    return if (path.endsWith(".svg", ignoreCase = true)) {
      path.dropLast(4)
    } else {
      path
    }
  }

  private fun sanitizeLocalName(input: String): String {
    return input
      .replace("/", "_")
      .replace("\\", "_")
      .replace("-", "_")
      .replace(Regex("[^a-zA-Z0-9_]"), "_")
      .replace(Regex("_+"), "_")
      .trim('_')
  }
}

public class StyleParamBuilder {
  internal val valuesList = mutableListOf<String>()

  public fun values(vararg values: String) {
    valuesList.addAll(values)
  }
}
