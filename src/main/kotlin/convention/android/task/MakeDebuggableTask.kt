package convention.android.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Adds `android:debuggable="true"` to the manifest. */
@DisableCachingByDefault(because = "Manifest transform is fast and its output depends on the local build")
public abstract class MakeDebuggableTask : DefaultTask() {

  @get:PathSensitive(PathSensitivity.RELATIVE)
  @get:InputFile
  public abstract val mergedManifest: RegularFileProperty

  @get:OutputFile
  public abstract val debuggableManifest: RegularFileProperty

  @TaskAction
  internal fun addDebuggableTag() {
    var manifest = mergedManifest.get().asFile.readText()
    manifest = if ("android:debuggable" in manifest) {
      manifest.replace(
        oldValue = "android:debuggable=\"false\"",
        newValue = "android:debuggable=\"true\"",
      )
    } else {
      manifest.replace(
        oldValue = "<application",
        newValue = "<application\n        android:debuggable=\"true\"",
      )
    }
    debuggableManifest.get().asFile.writeText(manifest)
  }
}
