package convention.swiftinterop.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

public abstract class GenerateSwiftPackageDefinitionTask : DefaultTask() {

  @get:Input
  public abstract val swiftInteropModuleName: Property<String>

  @get:Input
  public abstract val swiftToolsVersion: Property<String>

  @get:Input
  @get:Optional
  public abstract val iosVersion: Property<String>

  @get:Input
  @get:Optional
  public abstract val macosVersion: Property<String>

  @get:Input
  @get:Optional
  public abstract val tvosVersion: Property<String>

  @get:Input
  @get:Optional
  public abstract val watchosVersion: Property<String>

  @get:OutputDirectory
  public abstract val outputDirectory: DirectoryProperty

  @get:Internal
  public val swiftPackageFile: Provider<RegularFile> get() = outputDirectory.file("Package.swift")

  @TaskAction
  public fun generate() {
    outputDirectory.get().asFile.recreateDirectories()

    val swiftInteropModuleName = this@GenerateSwiftPackageDefinitionTask.swiftInteropModuleName.get()
    val platforms = listOfNotNull(
      iosVersion.orNull?.let { ".iOS(\"$it\")" },
      macosVersion.orNull?.let { ".macOS(\"$it\")" },
      tvosVersion.orNull?.let { ".tvOS(\"$it\")" },
      watchosVersion.orNull?.let { ".watchOS(\"$it\")" },
    ).joinToString(",")

    swiftPackageFile.get().asFile.writeText(
      """
        // swift-tools-version:${swiftToolsVersion.get()}
        import PackageDescription

        let package = Package(
          name: "$swiftInteropModuleName",
          platforms: [$platforms],
          products: [
            .library(
              name: "$swiftInteropModuleName",
              type: .static,
              targets: ["$swiftInteropModuleName"]
            )
          ],
          dependencies: [],
          targets: [
            .target(
              name: "$swiftInteropModuleName"
            )
          ]
        )
      """.trimIndent()
    )
  }
}
