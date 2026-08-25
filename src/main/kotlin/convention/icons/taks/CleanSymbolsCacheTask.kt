package convention.icons.taks

import convention.icons.utils.PathUtils
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Deleting the download cache is a cleanup action with no cacheable output")
public abstract class CleanSymbolsCacheTask : DefaultTask() {
  @get:Input
  public abstract val cacheDirectory: Property<String>

  @get:Input
  public abstract val projectBuildDir: Property<String>

  @TaskAction
  public fun clean() {
    val cacheDirPath = cacheDirectory.get()
    val projectBuildDirPath = projectBuildDir.get()

    val cacheBaseDir = PathUtils.resolveCacheDirectory(cacheDirPath, projectBuildDirPath)

    logger.lifecycle("🧹 Cleaning Material Symbols cache...")
    logger.lifecycle("📂 Cache location: ${cacheBaseDir.absolutePath}")

    if (cacheBaseDir.exists()) {
      val svgCacheDir = File(cacheBaseDir, "svg-cache")
      val tempSvgDir = File(cacheBaseDir, "temp-svgs")

      var deletedCount = 0

      if (svgCacheDir.exists()) {
        val fileCount = svgCacheDir.listFiles()?.size ?: 0
        if (svgCacheDir.deleteRecursively()) {
          deletedCount += fileCount
          logger.lifecycle("   🧹 Cleaned SVG cache: $fileCount files")
        } else {
          logger.warn(
            "   ⚠️ Failed to clean SVG cache directory: ${svgCacheDir.absolutePath}"
          )
        }
      }

      if (tempSvgDir.exists()) {
        val tempFiles = tempSvgDir.listFiles()
        deletedCount += tempFiles?.size ?: 0
        tempSvgDir.deleteRecursively()
        logger.lifecycle("   🧹 Cleaned temp SVGs: ${tempFiles?.size ?: 0} files")
      }
      
      if (cacheBaseDir.listFiles()?.isEmpty() == true) {
        cacheBaseDir.delete()
        logger.lifecycle("   🧹 Removed empty cache directory")
      }

      logger.lifecycle("✅ Total cache cleaned: $deletedCount files")
    } else {
      logger.lifecycle("ℹ️  No cache to clean (directory does not exist)")
    }
  }
}
