package convention.android.task

import convention.android.internal.createNewAppName
import java.io.File
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

  @get:Input
  public abstract val inputProjectName: Property<String>

  @get:Input
  public abstract val inputVariantName: Property<String>

  @TaskAction
  internal fun doTaskAction() {
    val manifestDir = inputManifestFile.get().asFile
    val stringsDir = inputStringsFile.get().asFile
    val bundleFile = inputBundleFile.get().asFile
    val mappingFile = inputMappingFile.get().asFile
    val rootDirectory = inputRootDirectory.get().asFile
    val projectName = inputProjectName.get()
    val variantName = inputVariantName.get()

    val deliverableDir = File(rootDirectory, "distributions/${projectName}/aab")
      .apply { if (!exists()) mkdirs() }
    val mappingDir = File(rootDirectory, "distributions/$projectName/mapping/$variantName")
      .apply { if (!exists()) mkdirs() }
    val newAppName = manifestDir.createNewAppName(stringsDir, variantName, "aab")

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
    } catch (e: Exception) {
      logger.error("❌ AAB renaming failed: ${e.message}", e)
      throw GradleException("Failed to rename AAB", e)
    }
  }
}
