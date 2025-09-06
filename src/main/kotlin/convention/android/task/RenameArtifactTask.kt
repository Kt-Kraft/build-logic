package convention.android.task

import com.android.build.api.variant.BuiltArtifactsLoader
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import net.pearx.kasechange.toSnakeCase
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
import org.w3c.dom.Document

private fun Document.getAppName(): String? {
  val stringElements = documentElement.getElementsByTagName("string")
  return (0 until stringElements.length).firstNotNullOfOrNull { index ->
    val node = stringElements.item(index)
    val nameAttribute = node?.attributes?.getNamedItem("name")?.nodeValue
    node?.textContent.takeIf { nameAttribute == "app_name" && !it.isNullOrBlank() }
  }
}

private fun Document.createSuffix(variant: String, extension: String): String {
  val versionCode = documentElement.getAttribute("android:versionCode")
  val versionName = documentElement.getAttribute("android:versionName")
  val formattedDateTime = LocalDateTime.now(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("ddMMMyyyy'T'HH.mm", Locale.getDefault()))
  return "V$versionName-$versionCode-$formattedDateTime-${variant.toSnakeCase().uppercase()}.$extension"
}

private fun createNewAppName(
  manifestDirectory: File,
  stringsDirectory: File,
  variantName: String,
  extension: String,
): String {
  val manifest = manifestDirectory.readText()
  val manifestDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    .parse(manifest.byteInputStream())
  val stringsFile = stringsDirectory.readText()
  val stringsDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    .parse(stringsFile.byteInputStream())
  val appName = stringsDocument.getAppName().orEmpty().replace(" ", "-")
  val suffix = manifestDocument.createSuffix(variant = variantName, extension = extension)
  return "$appName-$suffix"
}

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
    val newAppName = createNewAppName(manifestDir, stringsDir, variantName, "aab")

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
    val newAppName = createNewAppName(manifestDir, stringsDir, variantName, "apk")

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
