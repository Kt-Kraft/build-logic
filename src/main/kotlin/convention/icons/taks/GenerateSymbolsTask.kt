package convention.icons.taks

import convention.icons.download.SvgDownloader
import convention.icons.plugin.SymbolCraftExtension
import convention.icons.taks.internal.DownloadCoordinator
import convention.icons.taks.internal.GenerationContext
import convention.icons.taks.internal.GenerationContextFactory
import convention.icons.taks.internal.IconLibraryClassifier
import convention.icons.taks.internal.PreGenerationCleaner
import convention.icons.taks.internal.SvgConversionCoordinator
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class GenerateSymbolsTask : DefaultTask() {

  @get:Internal
  public abstract val extension: Property<SymbolCraftExtension>

  @get:Input
  public val symbolsConfigHash: String
    get() = extension.get().getConfigHash()

  @get:OutputDirectory
  public abstract val outputDir: DirectoryProperty

  @get:Input
  public abstract val cacheDirectory: Property<String>

  @get:Input
  public abstract val gradleUserHomeDir: Property<String>

  @get:Input
  public abstract val projectBuildDir: Property<String>

  @TaskAction
  public fun generate(): Unit = runBlocking {
    val resolvedExtension = extension.get()
    val context = buildContext(resolvedExtension)

    logGenerationStart(context)

    val iconsByLibrary = IconLibraryClassifier.groupByLibrary(context.config)
    logger.debug("📚 Libraries found: ${iconsByLibrary.keys.joinToString()}")

    val cleaner = PreGenerationCleaner(logger)
    cleaner.clean(context)

    val downloader = setupDownloader(context)
    val downloadCoordinator = DownloadCoordinator(logger)
    val conversionCoordinator = SvgConversionCoordinator(logger)

    try {
      downloadCoordinator.execute(downloader, context.config, context.tempDir)
      conversionCoordinator.convert(context, iconsByLibrary)
      downloadCoordinator.logCacheStatistics(downloader)
    } catch (e: Exception) {
      handleGenerationError(e)
    } finally {
      cleanupDownloader(downloader)
    }
  }

  private fun buildContext(ext: SymbolCraftExtension): GenerationContext {
    val contextFactory =
      GenerationContextFactory(
        extension = ext,
        outputDir = outputDir.get().asFile,
        cacheDirectory = cacheDirectory.get(),
        projectBuildDir = projectBuildDir.get(),
      )
    return contextFactory.create()
  }

  private fun logGenerationStart(context: GenerationContext) {
    val totalIcons = context.config.values.sumOf { it.size }
    logger.lifecycle("🎨 Generating icons...")
    logger.lifecycle("📊 Icons to generate: $totalIcons total")
    logger.debug("📂 Cache directory: ${context.cacheBaseDir.absolutePath}")
  }

  private fun setupDownloader(context: GenerationContext): SvgDownloader {
    return SvgDownloader(
      cacheDirectory = context.svgCacheDir.absolutePath,
      cacheEnabled = context.extension.cacheEnabled.get(),
      maxRetries = context.extension.maxRetries.get(),
      retryDelayMs = context.extension.retryDelayMs.get(),
      logger = { message -> logger.debug(message) },
    )
  }

  private fun handleGenerationError(e: Exception): Nothing {
    logger.error("❌ Generation failed: ${e.message}")
    logger.error("   Stack trace: ${e.stackTraceToString()}")

    val guidance =
      when {
        e.message?.contains("network", ignoreCase = true) == true ->
          "Network issue detected. Check internet connection and try again."

        e.message?.contains("cache", ignoreCase = true) == true ->
          "Cache issue detected. Try running with --rerun-tasks or clearing cache."

        e.message?.contains("SVG", ignoreCase = true) == true ->
          "SVG processing issue. Check if the requested icons exist in Material Symbols."

        else -> "Unexpected error. Please check configuration and try again."
      }

    logger.error("   💡 $guidance")
    throw e
  }

  private fun cleanupDownloader(downloader: SvgDownloader) {
    try {
      downloader.cleanup()
    } catch (cleanupException: Exception) {
      logger.warn("⚠️ Warning: Failed to cleanup downloader: ${cleanupException.message}")
    }
  }
}
