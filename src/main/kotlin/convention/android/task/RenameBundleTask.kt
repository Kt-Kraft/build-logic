package convention.android.task

import convention.android.internal.createNewAppName
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import net.pearx.kasechange.toPascalCase
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

public abstract class RenameBundleTask : DefaultTask() {

  @get:InputFile
  public abstract val inputBundleFile: RegularFileProperty

  @get:InputFile
  public abstract val inputStringsFile: RegularFileProperty

  @get:InputFile
  public abstract val inputManifestFile: RegularFileProperty

  @get:InputFile
  public abstract val inputMappingFile: RegularFileProperty

  @get:InputDirectory
  public abstract val inputRootDirectory: DirectoryProperty

  @get:InputDirectory
  public abstract val inputProjectBuildDirectory: DirectoryProperty

  @get:Input
  public abstract val inputProjectName: Property<String>

  @get:Input
  public abstract val inputVariantName: Property<String>

  @TaskAction
  internal fun doTaskAction() {
    val manifestFile = inputManifestFile.get().asFile
    val stringsFile = inputStringsFile.get().asFile
    val bundleFile = inputBundleFile.get().asFile
    val mappingFile = inputMappingFile.get().asFile
    val rootDirectory = inputRootDirectory.get().asFile
    val projectBuildDirectory = inputProjectBuildDirectory.get().asFile
    val projectName = inputProjectName.get()
    val variantName = inputVariantName.get()

    val newAppName = manifestFile.createNewAppName(stringsFile, variantName, "aab")
    val deliverableDir = File(rootDirectory, "distributions/${projectName}/aab")
      .apply { if (!exists()) mkdirs() }
    val mappingDir = File(rootDirectory, "distributions/$projectName/mapping/$variantName")
      .apply { if (!exists()) mkdirs() }
    val mergedNativeLibsDir = File(rootDirectory, "distributions/$projectName/merged_native_libs/$variantName")
      .apply { if (!exists()) mkdirs() }

    try {
      val newBundleFile = bundleFile.copyTo(File(deliverableDir, newAppName), overwrite = true)
      val renamedOutput = File(deliverableDir, "latest-deliverable-aab.txt")
      renamedOutput.writeText(newBundleFile.absolutePath)
      logger.lifecycle("✅ AAB renaming successful: ${newBundleFile.name}")
      logger.lifecycle("✅ AAB file copied → ${newBundleFile.absolutePath}")

      if (mappingFile.exists()) {
        val sourceMappingDir = mappingFile.parentFile
        sourceMappingDir.copyRecursively(mappingDir, overwrite = true)
        logger.lifecycle("✅ Mapping files copied → $mappingDir")
      }

      if (projectBuildDirectory.exists()) {
        val intermediatesBuildDir = File(
          projectBuildDirectory,
          "intermediates/merged_native_libs/$variantName/merge${variantName.toPascalCase()}NativeLibs/out/lib"
        )

        if (!intermediatesBuildDir.exists()) {
          logger.lifecycle("⚠️ No merged_native_libs found at $intermediatesBuildDir")
          return
        }

        intermediatesBuildDir.copyRecursively(mergedNativeLibsDir, overwrite = true)

        val zipFile = File(mergedNativeLibsDir.parentFile, "merged_native_libs-$variantName.zip")
        ZipOutputStream(zipFile.outputStream().buffered()).use { zipOut ->
          mergedNativeLibsDir.walkTopDown().forEach { file ->
            if (file.isFile) {
              val relativePath = file.relativeTo(mergedNativeLibsDir).path
              if (relativePath.startsWith("__MACOSX") || relativePath.endsWith(".DS_Store")) return@forEach

              val entry = ZipEntry(relativePath.replace(File.separatorChar, '/'))
              zipOut.putNextEntry(entry)
              file.inputStream().use { it.copyTo(zipOut) }
              zipOut.closeEntry()
            }
          }
        }

        logger.lifecycle("✅ merged_native_libs copied → $mergedNativeLibsDir")
        logger.lifecycle("✅ merged_native_libs zipped → $zipFile")
      }
    } catch (e: Exception) {
      logger.error("❌ AAB renaming failed: ${e.message}", e)
      throw GradleException("Failed to rename AAB", e)
    }
  }
}
