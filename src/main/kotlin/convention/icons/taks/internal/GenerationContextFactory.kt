package convention.icons.taks.internal

import convention.icons.model.IconConfig
import convention.icons.plugin.SymbolCraftExtension
import convention.icons.utils.PathUtils
import java.io.File

internal class GenerationContextFactory(
  private val extension: SymbolCraftExtension,
  private val outputDir: File,
  private val cacheDirectory: String,
  private val projectBuildDir: String,
) {

  fun create(): GenerationContext {
    val cacheBaseDir = PathUtils.resolveCacheDirectory(cacheDirectory, projectBuildDir)

    return GenerationContext(
      extension = extension,
      config = extension.getIconsConfig(),
      packageName = extension.packageName.get(),
      cacheBaseDir = cacheBaseDir,
      tempDir = File(cacheBaseDir, "temp-svgs"),
      svgCacheDir = File(cacheBaseDir, "svg-cache"),
      outputDir = outputDir,
      projectBuildDir = projectBuildDir,
    )
  }
}

public data class GenerationContext(
  val extension: SymbolCraftExtension,
  val config: Map<String, List<IconConfig>>,
  val packageName: String,
  val cacheBaseDir: File,
  val tempDir: File,
  val svgCacheDir: File,
  val outputDir: File,
  val projectBuildDir: String,
)
