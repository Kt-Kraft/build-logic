package convention.swiftinterop

import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_KOTLIN_MULTIPLATFORM
import convention.common.internal.applyPlugins
import convention.common.internal.requiredPlugin
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.internal.os.OperatingSystem

public abstract class SwiftInteropPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseConventionPlugin() {

  private val swiftInteropOptions: SwiftInteropExtension
    get() = conventionOptions.extensions.swiftInteropOptions

  private val isMacos = OperatingSystem.current().isMacOsX

  private fun Task.onlyIfMacos() {
    onlyIf { isMacos }
  }

  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_KOTLIN_MULTIPLATFORM,
      errorMessage = "Kotlin Multiplatform Gradle Plugin not found.",
    )
    applyPlugins(PLUGIN_ID_KOTLIN_MULTIPLATFORM)
    // configureMultiplatform(multiplatformOptionsExtension)
  }

  /*override fun apply(project: Project) {
    val swiftInterop = project.extensions.create("swiftInterop", SwiftInteropExtension::class.java)
    val buildDirectory = project.layout.buildDirectory.dir("swiftinterop")

    val generateSwiftCinteropDefinition = project.tasks.register(
      "generateSwiftCinteropDefinition",
      GenerateSwiftCinteropDefinitionTask::class.java
    ) {
      group = "swiftinterop"
      onlyIfMacos()

      swiftInteropModuleName.set(swiftInterop.swiftInteropModuleName)
      packageName.set(swiftInterop.packageName)
      iosVersion.set(swiftInterop.iosVersion)
      macosVersion.set(swiftInterop.macosVersion)
      tvosVersion.set(swiftInterop.tvosVersion)
      watchosVersion.set(swiftInterop.watchosVersion)
      outputDirectory.set(buildDirectory.map { it.dir("cinterop") })
    }
    val generateSwiftPackageDefinition = project.tasks.register(
      "generateSwiftPackageDefinition",
      GenerateSwiftPackageDefinitionTask::class.java
    ) {
      group = "swiftinterop"
      onlyIfMacos()

      swiftInteropModuleName.set(swiftInterop.swiftInteropModuleName)
      swiftToolsVersion.set(swiftInterop.swiftToolsVersion)
      iosVersion.set(swiftInterop.iosVersion)
      macosVersion.set(swiftInterop.macosVersion)
      tvosVersion.set(swiftInterop.tvosVersion)
      watchosVersion.set(swiftInterop.watchosVersion)
      outputDirectory.set(buildDirectory.map { it.dir("spm") })
    }

    val xcodebuildBuildOutputs = XcodebuildBuildTarget.Generic.entries.associateWith { target ->
      val targetOutputDir = buildDirectory.map { it.dir("xcodebuild/${target.disambiguationClassifier}") }

      targetOutputDir to project.tasks.register(
        "${target.disambiguationClassifier}XcodebuildBuild",
        XcodebuildBuildTask::class.java,
      ) {
        group = "swiftinterop"
        onlyIfMacos()

        swiftInteropModuleName.set(swiftInterop.swiftInteropModuleName)
        destination.set(target.destination)
        swiftPackageFile.set(generateSwiftPackageDefinition.map { it.swiftPackageFile.get() })
        swiftSources.from("src/commonMain/swift")
        outputDirectory.set(targetOutputDir)
      }
    }

    project.extensions.configure<KotlinMultiplatformExtension>("kotlin") {
      targets.withType(KotlinNativeTarget::class.java).all { nativeTarget ->
        if (!nativeTarget.konanTarget.family.isAppleFamily) return@all
        val targetInfo = XcodebuildBuildKonanTargetInfo(nativeTarget.konanTarget)
        val xcodebuildBuildOutput = xcodebuildBuildOutputs.getValue(targetInfo.target)

        val xcodeOutputDirectory = xcodebuildBuildOutput.first.map {
          val buildFolderName = "${swiftInterop.swiftInteropModuleName.get()}.build"
          it.dir("Build/Intermediates.noindex/$buildFolderName/${targetInfo.releaseFolder}/$buildFolderName/Objects-normal/${targetInfo.arch}")
        }

        val includeDirectory = buildDirectory.map { it.dir("outputs/${nativeTarget.disambiguationClassifier}/include") }
        val libsDirectory = buildDirectory.map { it.dir("outputs/${nativeTarget.disambiguationClassifier}/libs") }

        val copyObjectFiles = project.tasks.register(
          "${nativeTarget.disambiguationClassifier}CopyObjectFiles",
          Sync::class.java
        ) {
          group = "swiftinterop"
          onlyIfMacos()
          dependsOn(xcodebuildBuildOutput.second)

          from(xcodeOutputDirectory) {
            include("*.o")
          }
          into(buildDirectory.map { it.dir("outputs/${nativeTarget.disambiguationClassifier}/objects") })
        }

        val copyHeaderFiles = project.tasks.register(
          "${nativeTarget.disambiguationClassifier}CopyHeaderFiles",
          Sync::class.java
        ) {
          group = "swiftinterop"
          onlyIfMacos()
          dependsOn(xcodebuildBuildOutput.second)

          from(xcodeOutputDirectory) {
            include("*.h")
          }
          into(includeDirectory)
        }

        val libtoolBuildStatic = project.tasks.register(
          "${nativeTarget.disambiguationClassifier}LibtoolBuildStatic",
          LibtoolBuildStaticTask::class.java,
        ) {
          group = "swiftinterop"
          onlyIfMacos()

          swiftInteropModuleName.set(swiftInterop.swiftInteropModuleName)
          objectFiles.from(copyObjectFiles)
          outputDirectory.set(libsDirectory)
        }

        nativeTarget.compilations.getByName("main") { compilation ->
          compilation.cinterops.create("swiftinterop") { cinterop ->
            cinterop.definitionFile.set(generateSwiftCinteropDefinition.map { it.defFile.get() })
            cinterop.compilerOpts.add("-I${includeDirectory.get().asFile.absolutePath}")
            cinterop.extraOpts("-libraryPath", libsDirectory.get().asFile.absolutePath)

            project.tasks.named(cinterop.interopProcessingTaskName, CInteropProcess::class.java) {
              it.inputs.files(includeDirectory)
              it.inputs.files(libsDirectory)
              it.dependsOn(copyHeaderFiles, libtoolBuildStatic)
            }
          }
        }
      }
    }
  }*/
}
