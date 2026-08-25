package convention.icons.plugin

import convention.icons.taks.CleanSymbolsCacheTask
import convention.icons.taks.CleanSymbolsIconsTask
import convention.icons.taks.GenerateSymbolsTask
import convention.icons.taks.ValidateSymbolsConfigTask
import org.gradle.api.Plugin
import org.gradle.api.Project

public class SymbolCraftPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val symbolCraftExtension = project.extensions.create("symbolCraft", SymbolCraftExtension::class.java)
    symbolCraftExtension.projectDirectory.set(project.layout.projectDirectory.asFile.absolutePath)

    project.tasks.register("generateSymbolCraftIcons", GenerateSymbolsTask::class.java) {
      group = "SymbolCraft"
      description = "Generate icons from configured libraries"
      extension.set(symbolCraftExtension)
      outputDir.set(project.layout.projectDirectory.dir(symbolCraftExtension.outputDirectory))
      cacheDirectory.set(symbolCraftExtension.cacheDirectory)
      gradleUserHomeDir.set(project.gradle.gradleUserHomeDir.absolutePath)
      projectBuildDir.set(project.layout.buildDirectory.get().asFile.absolutePath)
      inputs.property("symbolsConfig", symbolCraftExtension.getConfigHash())
      inputs.property("generatePreview", symbolCraftExtension.generatePreview)
      inputs.property("namingConfigSignature", symbolCraftExtension.namingConfigSignature())
    }

    project.tasks.register("cleanSymbolCraftCache", CleanSymbolsCacheTask::class.java) {
      group = "SymbolCraft"
      description = "Clean SymbolCraft icon cache"
      cacheDirectory.set(symbolCraftExtension.cacheDirectory)
      projectBuildDir.set(project.layout.buildDirectory.get().asFile.absolutePath)
    }

    project.tasks.register("cleanSymbolCraftIcons", CleanSymbolsIconsTask::class.java) {
      group = "SymbolCraft"
      description = "Clean generated SymbolCraft icon files"
      packageName.set(symbolCraftExtension.packageName)
      outputDirectory.set(project.layout.projectDirectory.dir(symbolCraftExtension.outputDirectory))
    }

    project.tasks.register(
      "validateSymbolCraftConfig",
      ValidateSymbolsConfigTask::class.java,
    ) {
      group = "SymbolCraft"
      description = "Validate SymbolCraft icon configuration"
      extension.set(symbolCraftExtension)
    }
  }
}
