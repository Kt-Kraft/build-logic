package convention.android.task

import com.android.build.api.variant.BuiltArtifactsLoader
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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

public abstract class RenameApkTask : DefaultTask() {

  @get:Internal
  public abstract val mBuiltArtifactsLoader: Property<BuiltArtifactsLoader>

  @get:InputDirectory
  public abstract val inputApkDirectory: DirectoryProperty

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
    val builtArtifacts = mBuiltArtifactsLoader.get()
      .load(inputApkDirectory.get())

    checkNotNull(builtArtifacts) {
      "Can't load APKs"
    }

    check(builtArtifacts.elements.size == 1) {
      "Expected one APK!"
    }

    val manifestDir = inputManifestFile.get().asFile
    val stringsDir = inputStringsFile.get().asFile
    val mappingFile = inputMappingFile.get().asFile
    val rootDirectory = inputRootDirectory.get().asFile
    val projectName = inputProjectName.get()
    val variantName = inputVariantName.get()

    val deliverableDir = File(rootDirectory, "distributions/${projectName}/apk")
      .apply { if (!exists()) mkdirs() }
    val mappingDir = File(rootDirectory, "distributions/$projectName/mapping/$variantName")
      .apply { if (!exists()) mkdirs() }
    val newAppName = manifestDir.createNewAppName(stringsDir, variantName, "apk")

    try {
      val apkFile = File(builtArtifacts.elements.single().outputFile)
      val newApkFile = apkFile.copyTo(File(deliverableDir, newAppName), overwrite = true)
      val renamedOutput = File(deliverableDir, "latest-deliverable-apk.txt")
      renamedOutput.writeText(newApkFile.absolutePath)
      logger.lifecycle("✅ APK renaming successful: ${newApkFile.name}")
      logger.lifecycle("✅ APK file copied → ${newApkFile.absolutePath}")

      if (mappingFile.exists()) {
        val sourceMappingDir = mappingFile.parentFile
        sourceMappingDir.copyRecursively(mappingDir, overwrite = true)
        logger.lifecycle("✅ Mapping files copied → $mappingDir")
      }
    } catch (e: Exception) {
      logger.error("❌ APK renaming failed: ${e.message}", e)
      throw GradleException("Failed to rename APK", e)
    }
  }
}
