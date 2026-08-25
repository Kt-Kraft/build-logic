package convention.swiftinterop.tasks

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Output depends on the local Xcode toolchain")
public abstract class LibtoolBuildStaticTask : DefaultTask() {

  @get:Input
  public abstract val swiftInteropModuleName: Property<String>

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  public abstract val objectFiles: ConfigurableFileCollection

  @get:OutputDirectory
  public abstract val outputDirectory: DirectoryProperty

  @get:Inject
  public abstract val exec: ExecOperations

  @TaskAction
  public fun build() {
    outputDirectory.get().asFile.recreateDirectories()

    exec.exec {
      commandLine(
        "libtool", "-static",
        "-o", outputDirectory.get().asFile.resolve("libswiftinterop_${swiftInteropModuleName.get()}.a").absolutePath,
        *objectFiles.asFileTree.map { it.absolutePath }.toTypedArray()
      )
    }
  }
}
