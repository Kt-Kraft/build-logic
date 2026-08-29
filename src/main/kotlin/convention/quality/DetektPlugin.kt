package convention.quality

import convention.common.BaseConventionPlugin
import convention.common.ConventionOptionsExtension
import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_DETEKT
import convention.common.internal.applyPlugins
import convention.common.internal.requiredPlugin
import dev.detekt.gradle.extensions.DetektExtension
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.kotlin.dsl.configure

public open class DetektPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseConventionPlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_DETEKT,
      errorMessage = "Detekt Gradle Plugin not found.",
    )

    val detektOptions = createExtension(
      name = DetektOptionsExtension.NAME,
      publicType = DetektOptionsExtension::class,
    )

    allprojects {
      applyPlugins(PLUGIN_ID_DETEKT)
      configureDetekt(detektOptions, conventionOptions)
    }

    registerAggregateTasks()
  }
}

private fun Project.configureDetekt(
  detektOptions: DetektOptionsExtension,
  conventionOptions: ConventionOptionsExtension,
) = extensions.configure<DetektExtension> {
  buildUponDefaultConfig.set(detektOptions.buildUponDefaultConfig)
  parallel.set(detektOptions.parallel)
  autoCorrect.set(detektOptions.autoCorrect)

  val configFile = conventionOptions.configsDir.file(detektOptions.configFileName.get()).get().asFile
  if (configFile.isFile) {
    config.setFrom(configFile)
  }
}

private fun String.isDetektAnalysisTask(): Boolean {
  return startsWith("detekt") &&
    !startsWith("detektBaseline") &&
    !startsWith("detektGenerateConfig") &&
    this != "detektAll"
}

private fun Project.registerAggregateTasks() {
  val detektAll = tasks.register("detektAll") {
    group = "verification"
    description = "Run detekt on all source sets"
    allprojects.forEach { project ->
      dependsOn(provider { project.tasks.filter { it.name.isDetektAnalysisTask() } })
    }
  }

  tasks.register("lintKotlin") {
    group = "verification"
    description = "Run Kotlin linter (detekt)"
    dependsOn(detektAll)
  }

  tasks.register("checkAll") {
    group = "verification"
    description = "Run all checks (format + lint + tests)"
    dependsOn(detektAll)
    dependsOn(provider { tasks.filter { it.name == "spotlessCheck" } })
    allprojects.forEach { project ->
      dependsOn(provider { project.tasks.filter { it.name == "test" || it.name == "allTests" } })
    }
  }
}
