package convention.icons.taks.internal

import convention.icons.download.SvgDownloader
import convention.icons.model.IconConfig
import convention.icons.model.LocalIconConfig
import java.io.File
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.gradle.api.logging.Logger

internal class DownloadCoordinator(
  private val logger: Logger,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

  suspend fun execute(downloader: SvgDownloader, config: Map<String, List<IconConfig>>, tempDir: File): DownloadStats {
    prepareTempDirectory(tempDir)
    val stats = downloadSvgsParallel(downloader, config, tempDir)
    logDownloadStats(stats)
    return stats
  }

  fun logCacheStatistics(downloader: SvgDownloader) {
    val cacheStats = downloader.getCacheStats()
    logger.lifecycle(
      "📦 SVG Cache: ${cacheStats.fileCount} files, ${
        String.format(
          Locale.getDefault(),
          "%.2f",
          cacheStats.totalSizeMB,
        )
      } MB"
    )
  }

  private fun prepareTempDirectory(tempDir: File) {
    if (tempDir.exists()) {
      tempDir.deleteRecursively()
    }
    tempDir.mkdirs()
  }

  private suspend fun downloadSvgsParallel(
    downloader: SvgDownloader,
    config: Map<String, List<IconConfig>>,
    tempDir: File,
  ): DownloadStats = coroutineScope {
    val totalIcons = config.values.sumOf { it.size }
    val remoteIconCount =
      config.values.sumOf { iconConfigs -> iconConfigs.count { it !is LocalIconConfig } }
    val localIconCount = totalIcons - remoteIconCount

    val completed = AtomicInteger(0)
    val failed = AtomicInteger(0)
    val cached = AtomicInteger(0)
    val localProcessed = AtomicInteger(0)

    if (remoteIconCount > 0) {
      logger.lifecycle("⬇️ Downloading SVG files...")
    }
    if (localIconCount > 0) {
      logger.lifecycle("📂 Processing local SVG files...")
    }

    val downloadJobs =
      config.flatMap { (iconName, iconConfigs) ->
        iconConfigs.map { iconConfig ->
          async(dispatcher) {
            when (iconConfig) {
              is LocalIconConfig -> {
                val result =
                  processLocalSvg(
                    iconName = iconName,
                    iconConfig = iconConfig,
                    tempDir = tempDir,
                    completed = localProcessed,
                    failed = failed,
                    totalCount = localIconCount,
                  )
                result
              }

              else -> {
                val result =
                  processRemoteSvg(
                    iconName = iconName,
                    iconConfig = iconConfig,
                    downloader = downloader,
                    tempDir = tempDir,
                    completed = completed,
                    failed = failed,
                    cached = cached,
                    totalCount = remoteIconCount,
                  )
                result
              }
            }
          }
        }
      }

    val results = downloadJobs.awaitAll()

    DownloadStats(
      totalCount = totalIcons,
      successCount = completed.get() + localProcessed.get(),
      failedCount = failed.get(),
      cachedCount = cached.get(),
      results = results,
    )
  }

  private fun processLocalSvg(
    iconName: String,
    iconConfig: LocalIconConfig,
    tempDir: File,
    completed: AtomicInteger,
    failed: AtomicInteger,
    totalCount: Int,
  ): DownloadResult {
    return try {
      val sourceFile = File(iconConfig.absolutePath)
      if (!sourceFile.exists() || !sourceFile.isFile) {
        failed.incrementAndGet()
        val message = "Local SVG not found at ${sourceFile.absolutePath}"
        logger.warn("   ⚠️ Failed to load local icon $iconName: $message")
        DownloadResult.Failed(iconName, iconConfig, message)
      } else {
        val librarySubdir = File(tempDir, iconConfig.libraryId)
        librarySubdir.mkdirs()

        val fileName = buildTempSvgFileName(iconName, iconConfig)
        val targetFile = File(librarySubdir, fileName)
        sourceFile.copyTo(targetFile, overwrite = true)

        val progress = completed.incrementAndGet()
        maybeLogProgress("Local progress", progress, totalCount)
        DownloadResult.Success(iconName, iconConfig, fileName)
      }
    } catch (e: Exception) {
      failed.incrementAndGet()
      val message = e.message ?: "Unknown error"
      logger.warn("   ❌ Error processing local icon $iconName: $message")
      DownloadResult.Failed(iconName, iconConfig, message)
    }
  }

  /**
   * Fetches a remote SVG through the shared [SvgDownloader].
   *
   * The downloader already handles retries and content validation; here we simply persist the
   * content into the temp workspace and update our counters.
   */
  private suspend fun processRemoteSvg(
    iconName: String,
    iconConfig: IconConfig,
    downloader: SvgDownloader,
    tempDir: File,
    completed: AtomicInteger,
    failed: AtomicInteger,
    cached: AtomicInteger,
    totalCount: Int,
  ): DownloadResult {
    return try {
      val cacheKey = iconConfig.getCacheKey(iconName)
      val wasCached = downloader.isCached(cacheKey)

      val svgContent = downloader.downloadSvg(iconName, iconConfig)

      if (svgContent != null && svgContent.isNotBlank()) {
        val librarySubdir = File(tempDir, iconConfig.libraryId)
        librarySubdir.mkdirs()

        val fileName = buildTempSvgFileName(iconName, iconConfig)
        val tempFile = File(librarySubdir, fileName)
        tempFile.writeText(svgContent)

        val progress = completed.incrementAndGet()
        if (wasCached) cached.incrementAndGet()
        maybeLogProgress("   Download progress", progress, totalCount)

        DownloadResult.Success(iconName, iconConfig, fileName)
      } else {
        failed.incrementAndGet()
        val errorMsg =
          if (svgContent == null) "Download returned null" else "Empty SVG content"
        logger.warn(
          "   ⚠️ Failed to download: $iconName-${iconConfig.getSignature()} ($errorMsg)"
        )
        DownloadResult.Failed(iconName, iconConfig, errorMsg)
      }
    } catch (e: Exception) {
      failed.incrementAndGet()
      val detailedError =
        when {
          e.message?.contains("timeout", ignoreCase = true) == true ->
            "Timeout - network too slow"

          e.message?.contains("404", ignoreCase = true) == true -> "Icon not found"
          e.message?.contains("connection", ignoreCase = true) == true ->
            "Network connection failed"

          else -> e.message ?: "Unknown error"
        }
      logger.warn(
        "   ❌ Error downloading $iconName-${iconConfig.getSignature()}: $detailedError"
      )
      DownloadResult.Failed(iconName, iconConfig, detailedError)
    }
  }

  private fun buildTempSvgFileName(iconName: String, iconConfig: IconConfig): String {
    val signature = iconConfig.getSignature()

    if (iconConfig is LocalIconConfig) {
      val preferredName = signature.ifBlank { iconName }
      val safeName =
        preferredName
          .replace("[^A-Za-z0-9_]".toRegex(), "_")
          .replace("_+".toRegex(), "_")
          .trim('_')
          .ifBlank { "LocalIcon" }
      return "$safeName.svg"
    }

    val base =
      iconName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val suffix = signature.ifBlank { "Local" }
    return "$base$suffix.svg"
  }

  private fun logDownloadStats(stats: DownloadStats) {
    logger.lifecycle("✅ Processing completed:")
    logger.lifecycle("   📁 Total: ${stats.totalCount}")
    logger.lifecycle("   ✅ Success: ${stats.successCount}")
    logger.lifecycle("   ❌ Failed: ${stats.failedCount}")

    if (stats.cachedCount > 0) {
      logger.lifecycle("   💾 From cache: ${stats.cachedCount} (remote icons only)")
    }

    if (stats.failedCount > 0) {
      logger.warn(
        "⚠️ Some icons failed to process. Generated code may use fallback implementations."
      )
    }
  }

  private fun maybeLogProgress(prefix: String, current: Int, total: Int) {
    if (total <= 0) return
    if (current == total || current % PROGRESS_STEP == 0) {
      val normalizedPrefix = if (prefix.startsWith("   ")) prefix else "   $prefix"
      logger.lifecycle("$normalizedPrefix: $current/$total")
    }
  }

  private companion object {
    private const val PROGRESS_STEP = 5
  }
}
