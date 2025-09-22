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

public abstract class GenerateSwiftCinteropDefinitionTask : DefaultTask() {

  @get:Input
  public abstract val swiftInteropModuleName: Property<String>

  @get:Input
  public abstract val packageName: Property<String>

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
  public val defFile: Provider<RegularFile> get() = outputDirectory.file("${swiftInteropModuleName.get()}.def")

  @TaskAction
  public fun generate() {
    outputDirectory.get().asFile.recreateDirectories()

    defFile.get().asFile.writeText(
      """
        language = Objective-C
        package = ${packageName.get()}
        headers = ${swiftInteropModuleName.get()}-Swift.h
        staticLibraries = libswiftinterop_${swiftInteropModuleName.get()}.a

        # linker options for Swift
        linkerOpts.osx = ${linkerOpts("macos", "macosx", macosVersion)}

        linkerOpts.ios_arm64 = ${linkerOpts("ios", "iphoneos", iosVersion)}
        linkerOpts.ios_x64 = ${linkerOpts("ios-simulator", "iphonesimulator", iosVersion)}
        linkerOpts.ios_simulator_arm64 = ${linkerOpts("ios-simulator", "iphonesimulator", iosVersion)}

        linkerOpts.watchos_arm32 = ${linkerOpts("watchos", "watchos", watchosVersion)}
        linkerOpts.watchos_arm64 = ${linkerOpts("watchos", "watchos", watchosVersion)}
        linkerOpts.watchos_device_arm64 = ${linkerOpts("watchos", "watchos", watchosVersion)}
        linkerOpts.watchos_x64 = ${linkerOpts("watchos-simulator", "watchsimulator", watchosVersion)}
        linkerOpts.watchos_simulator_arm64 = ${linkerOpts("watchos-simulator", "watchsimulator", watchosVersion)}

        linkerOpts.tvos_arm64 = ${linkerOpts("tvos", "appletvos", tvosVersion)}
        linkerOpts.tvos_x64 = ${linkerOpts("tvos-simulator", "appletvsimulator", tvosVersion)}
        linkerOpts.tvos_simulator_arm64 = ${linkerOpts("tvos-simulator", "appletvsimulator", tvosVersion)}
      """.trimIndent()
    )
  }

  private fun linkerOpts(
    os: String,
    libsDir: String,
    version: Provider<String>,
  ): String {
    val linker =
      "-L/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/$libsDir/"
    val v = version.orNull ?: return linker
    return "-platform_version $os ${v}.0 ${v}.0 $linker"
  }
}
