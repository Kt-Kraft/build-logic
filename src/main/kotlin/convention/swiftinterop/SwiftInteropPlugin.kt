package convention.swiftinterop

import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_KOTLIN_MULTIPLATFORM
import convention.common.internal.applyPlugins
import convention.common.internal.requiredPlugin
import convention.swiftinterop.tasks.GenerateSwiftCinteropDefinitionTask
import convention.swiftinterop.tasks.GenerateSwiftPackageDefinitionTask
import convention.swiftinterop.tasks.LibtoolBuildStaticTask
import convention.swiftinterop.tasks.XcodebuildBuildKonanTargetInfo
import convention.swiftinterop.tasks.XcodebuildBuildTarget
import convention.swiftinterop.tasks.XcodebuildBuildTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.api.tasks.Sync
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess

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
    configureSwiftInterop(swiftInteropOptions)
  }

  @OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
  private fun Project.configureSwiftInterop(
    swiftInteropOptions: SwiftInteropExtension
  ) = extensions.configure<KotlinMultiplatformExtension> {
    val buildDirectory = layout.buildDirectory.dir("swiftInterop")

    val generateSwiftCinteropDefinition = tasks.register(
      "generateSwiftCinteropDefinition",
      GenerateSwiftCinteropDefinitionTask::class.java
    ) {
      group = "swiftInterop"
      onlyIfMacos()

      swiftInteropModuleName.set(swiftInteropOptions.swiftInteropModuleName)
      packageName.set(swiftInteropOptions.packageName)
      iosVersion.set(swiftInteropOptions.iosVersion)
      macosVersion.set(swiftInteropOptions.macosVersion)
      tvosVersion.set(swiftInteropOptions.tvosVersion)
      watchosVersion.set(swiftInteropOptions.watchosVersion)
      outputDirectory.set(buildDirectory.map { it.dir("cinterop") })
    }
    val generateSwiftPackageDefinition = project.tasks.register(
      "generateSwiftPackageDefinition",
      GenerateSwiftPackageDefinitionTask::class.java
    ) {
      group = "swiftInterop"
      onlyIfMacos()

      swiftInteropModuleName.set(swiftInteropOptions.swiftInteropModuleName)
      swiftToolsVersion.set(swiftInteropOptions.swiftToolsVersion)
      iosVersion.set(swiftInteropOptions.iosVersion)
      macosVersion.set(swiftInteropOptions.macosVersion)
      tvosVersion.set(swiftInteropOptions.tvosVersion)
      watchosVersion.set(swiftInteropOptions.watchosVersion)
      outputDirectory.set(buildDirectory.map { it.dir("spm") })
    }

    val xcodebuildBuildOutputs = XcodebuildBuildTarget.Generic.entries.associateWith { target ->
      val targetOutputDir = buildDirectory.map { it.dir("xcodebuild/${target.disambiguationClassifier}") }

      targetOutputDir to project.tasks.register(
        "${target.disambiguationClassifier}XcodebuildBuild",
        XcodebuildBuildTask::class.java,
      ) {
        group = "swiftInterop"
        onlyIfMacos()

        swiftInteropModuleName.set(swiftInteropOptions.swiftInteropModuleName)
        destination.set(target.destination)
        swiftPackageFile.set(generateSwiftPackageDefinition.map { it.swiftPackageFile.get() })
        swiftSources.from("src/commonMain/swift")
        outputDirectory.set(targetOutputDir)
      }
    }

    targets.withType<KotlinNativeTarget>().configureEach {
      if (!konanTarget.family.isAppleFamily) return@configureEach

      val targetInfo = XcodebuildBuildKonanTargetInfo(konanTarget)
      val xcodebuildBuildOutput = xcodebuildBuildOutputs.getValue(targetInfo.target)

      val xcodeOutputDirectory = xcodebuildBuildOutput.first.map {
        val buildFolderName = "${swiftInteropOptions.swiftInteropModuleName.get()}.build"
        it.dir("Build/Intermediates.noindex/$buildFolderName/${targetInfo.releaseFolder}/$buildFolderName/Objects-normal/${targetInfo.arch}")
      }

      val includeDirectory = buildDirectory.map { it.dir("outputs/${disambiguationClassifier}/include") }
      val libsDirectory = buildDirectory.map { it.dir("outputs/${disambiguationClassifier}/libs") }

      val copyObjectFiles = project.tasks.register(
        "${disambiguationClassifier}CopyObjectFiles",
        Sync::class.java
      ) {
        group = "swiftinterop"
        onlyIfMacos()
        dependsOn(xcodebuildBuildOutput.second)

        from(xcodeOutputDirectory) {
          include("*.o")
        }
        into(buildDirectory.map { it.dir("outputs/${disambiguationClassifier}/objects") })
      }

      val copyHeaderFiles = project.tasks.register(
        "${disambiguationClassifier}CopyHeaderFiles",
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
        "${disambiguationClassifier}LibtoolBuildStatic",
        LibtoolBuildStaticTask::class.java,
      ) {
        group = "swiftinterop"
        onlyIfMacos()

        swiftInteropModuleName.set(swiftInteropOptions.swiftInteropModuleName)
        objectFiles.from(copyObjectFiles)
        outputDirectory.set(libsDirectory)
      }

      compilations.getByName("main") {
        cinterops.create("swiftinterop") {
          definitionFile.set(generateSwiftCinteropDefinition.map { it.defFile.get() })
          compilerOpts.add("-I${includeDirectory.get().asFile.absolutePath}")
          extraOpts("-libraryPath", libsDirectory.get().asFile.absolutePath)

          project.tasks.named(interopProcessingTaskName, CInteropProcess::class.java) {
            inputs.files(includeDirectory)
            inputs.files(libsDirectory)
            dependsOn(copyHeaderFiles, libtoolBuildStatic)
          }
        }
      }
    }
  }
}
