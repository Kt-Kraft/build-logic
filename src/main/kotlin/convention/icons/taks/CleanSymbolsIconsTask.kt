package convention.icons.taks

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Deleting generated icons is a cleanup action with no cacheable output")
public abstract class CleanSymbolsIconsTask : DefaultTask() {

  @get:Input
  public abstract val packageName: Property<String>

  @get:PathSensitive(PathSensitivity.RELATIVE)
  @get:InputDirectory
  @get:Optional
  public abstract val outputDirectory: DirectoryProperty

  @TaskAction
  public fun clean() {
    if (!outputDirectory.isPresent) {
      logger.lifecycle("ℹ️ Output directory not configured, skipping clean.")
      return
    }
    val pkgName = packageName.get()
    val outputDir = outputDirectory.get().asFile
    val packagePath = pkgName.replace('.', '/')
    val symbolsDir = File(outputDir, "$packagePath/icons")
    val mainSymbolsFile = File(outputDir, "$packagePath/__Icons.kt")

    var deletedCount = 0

    if (symbolsDir.exists()) {
      symbolsDir
        .walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .forEach { file ->
          logger.debug("🧹 Deleting generated file: ${file.relativeTo(symbolsDir).path}")
          if (file.delete()) {
            deletedCount++
          } else {
            logger.warn("   ⚠️ Failed to delete: ${file.absolutePath}")
          }
        }

      symbolsDir
        .walkBottomUp()
        .filter { it.isDirectory && it != symbolsDir && it.listFiles()?.isEmpty() == true }
        .forEach { dir ->
          logger.debug("🧹 Removing empty directory: ${dir.name}")
          dir.delete()
        }

      if (symbolsDir.listFiles()?.isEmpty() == true) {
        symbolsDir.delete()
        logger.debug("🧹 Removed empty icons directory")
      }
    }

    if (mainSymbolsFile.exists()) {
      if (mainSymbolsFile.delete()) {
        deletedCount++
        logger.debug("🧹 Deleted main symbols file")
      } else {
        logger.warn(
          "   ⚠️ Failed to delete main symbols file: ${mainSymbolsFile.absolutePath}"
        )
      }
    }

    logger.lifecycle("🧹 Cleaned $deletedCount generated icon files")
  }
}
