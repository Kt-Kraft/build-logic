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
import org.w3c.dom.Element

private fun Document.createNewName(extension: String, variant: String): String {
  val versionCode = documentElement.getAttribute("android:versionCode")
  val versionName = documentElement.getAttribute("android:versionName")
  val appLabel = documentElement.getElementsByTagName("application")
    .let { it.item(0) as Element }.getAttribute("android:label")
    .replace(" ", "-")

  val formattedDateTime = LocalDateTime.now(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("ddMMMyyyy'T'HH.mm", Locale.getDefault()))
  return "$appLabel-V$versionName-$versionCode-$formattedDateTime-${variant.uppercase()}.$extension"
}

/**
 * This abstract class represents a task for renaming Android App Bundles (AABs).
 * It extends the DefaultTask class provided by Gradle.
 *
 * @property inputBundleDirectory The directory containing the input bundle file. This is an abstract property that must be implemented by subclasses.
 * @property inputManifestDirectory The directory containing the input Android manifest file. This is an abstract property that must be implemented by subclasses.
 */
public abstract class RenameBundleTask : DefaultTask() {

  /**
   * The directory containing the input bundle file.
   */
  @get:InputFile
  public abstract val inputBundleDirectory: RegularFileProperty

  /**
   * The directory containing the input Android manifest file.
   */
  @get:InputFile
  public abstract val inputManifestDirectory: RegularFileProperty

  @get:Input
  public abstract val variantName: Property<String>

  @TaskAction
  internal fun doTaskAction() {
    val manifest = inputManifestDirectory.get().asFile.readText()
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
      .parse(manifest.byteInputStream())
    val applicationName = document.createNewName(extension = "aab", variant = variantName.get())

    try {
      val bundleFile = inputBundleDirectory.get().asFile
      val newApkFile = bundleFile.copyTo(File(bundleFile.parent, "deliverable/${applicationName}"), overwrite = true)
      val renamedOutput = File(bundleFile.parent, "deliverable-aab.txt")
      renamedOutput.writeText(newApkFile.toPath().toString())
      println("APK renaming successful: $applicationName")
    } catch (_: Exception) {
      println("APK renaming failed.")
    }
  }
}

/**
 * This abstract class represents a task for renaming Android APKs.
 * It extends the DefaultTask class provided by Gradle.
 *
 * @property mBuiltArtifactsLoader The loader for built artifacts. This is an abstract property that must be implemented by subclasses.
 * @property inputApkDirectory The directory containing the input APK file. This is an abstract property that must be implemented by subclasses.
 * @property inputManifestDirectory The directory containing the input Android manifest file. This is an abstract property that must be implemented by subclasses.
 */
public abstract class RenameApkTask : DefaultTask() {

  /**
   * The loader for built artifacts.
   */
  @get:Internal
  public abstract val mBuiltArtifactsLoader: Property<BuiltArtifactsLoader>

  /**
   * The directory containing the input APK file.
   */
  @get:InputDirectory
  public abstract val inputApkDirectory: DirectoryProperty

  /**
   * The directory containing the input Android manifest file.
   */
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

    val manifest = inputManifestDirectory.get().asFile.readText()
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
      .parse(manifest.byteInputStream())
    val applicationName = document.createNewName(extension = "apk", variant = variantName.get())

    try {
      val apkFile = File(builtArtifacts.elements.single().outputFile)
      val newApkFile = apkFile.copyTo(File(apkFile.parent, "deliverable/${applicationName}"), overwrite = true)
      val renamedOutput = File(apkFile.parent, "deliverable-apk.txt")
      renamedOutput.writeText(newApkFile.toPath().toString())
      println("APK renaming successful: $applicationName")
    } catch (_: Exception) {
      println("APK renaming failed.")
    }
  }
}
