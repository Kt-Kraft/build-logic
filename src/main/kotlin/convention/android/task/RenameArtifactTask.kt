package convention.android.task

import com.android.build.api.variant.BuiltArtifactsLoader
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.DefaultTask
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
  return "V$versionName-$versionCode-$formattedDateTime-${variant.uppercase()}.$extension"
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
  public abstract val inputBundleDirectory: RegularFileProperty

  @get:InputFile
  public abstract val inputStringsDirectory: RegularFileProperty

  @get:InputFile
  public abstract val inputManifestDirectory: RegularFileProperty

  @get:Input
  public abstract val variantName: Property<String>

  @TaskAction
  internal fun doTaskAction() {
    val newAppName = createNewAppName(
      inputManifestDirectory.get().asFile,
      inputStringsDirectory.get().asFile,
      variantName.get(),
      "aab"
    )
    try {
      val bundleFile = inputBundleDirectory.get().asFile
      val newApkFile = bundleFile.copyTo(File(bundleFile.parent, "deliverable/$newAppName"), overwrite = true)
      val renamedOutput = File(bundleFile.parent, "deliverable-aab.txt")
      renamedOutput.writeText(newApkFile.toPath().toString())
      println("APK renaming successful: $newAppName")
    } catch (_: Exception) {
      println("APK renaming failed.")
    }
  }
}

public abstract class RenameApkTask : DefaultTask() {

  @get:Internal
  public abstract val mBuiltArtifactsLoader: Property<BuiltArtifactsLoader>

  @get:InputDirectory
  public abstract val inputApkDirectory: DirectoryProperty

  @get:InputFile
  public abstract val inputStringsDirectory: RegularFileProperty

  @get:InputFile
  public abstract val inputManifestDirectory: RegularFileProperty

  @get:Input
  public abstract val variantName: Property<String>

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

    val newAppName = createNewAppName(
      inputManifestDirectory.get().asFile,
      inputStringsDirectory.get().asFile,
      variantName.get(),
      "apk"
    )

    try {
      val apkFile = File(builtArtifacts.elements.single().outputFile)
      val newApkFile = apkFile.copyTo(File(apkFile.parent, "deliverable/$newAppName"), overwrite = true)
      val renamedOutput = File(apkFile.parent, "deliverable-apk.txt")
      renamedOutput.writeText(newApkFile.toPath().toString())
      println("APK renaming successful: $newAppName")
    } catch (_: Exception) {
      println("APK renaming failed.")
    }
  }
}
