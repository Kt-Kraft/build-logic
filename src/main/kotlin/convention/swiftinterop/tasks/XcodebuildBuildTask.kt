package convention.swiftinterop.tasks

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

public abstract class XcodebuildBuildTask : DefaultTask() {

  @get:Input
  public abstract val swiftInteropModuleName: Property<String>

  @get:Input
  public abstract val destination: Property<String>

  @get:InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  public abstract val swiftPackageFile: RegularFileProperty

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  public abstract val swiftSources: ConfigurableFileCollection

  @get:OutputDirectory
  public abstract val outputDirectory: DirectoryProperty

  @get:Inject
  public abstract val exec: ExecOperations

  @TaskAction
  public fun build() {
    outputDirectory.get().asFile.recreateDirectories()
    temporaryDir.recreateDirectories()

    swiftPackageFile.get().asFile.copyTo(temporaryDir.resolve("Package.swift"))
    swiftSources.asFileTree.forEach {
      it.copyTo(temporaryDir.resolve("Sources").resolve(swiftInteropModuleName.get()).resolve(it.name))
    }

    exec.exec {
      workingDir = temporaryDir
      commandLine(
        "xcodebuild", "build",
        "-scheme", swiftInteropModuleName.get(),
        "-configuration", "Release",
        "-derivedDataPath", outputDirectory.get().asFile.absolutePath,
        "-destination", destination.get()
      )
    }
  }
}
