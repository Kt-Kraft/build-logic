package convention.quality

import com.autonomousapps.DependencyAnalysisExtension
import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_DEPENDENCY_ANALYSIS
import convention.common.internal.applyPlugins
import convention.common.internal.requiredPlugin
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.kotlin.dsl.configure

public open class DependencyAnalysisPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseConventionPlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_DEPENDENCY_ANALYSIS,
      errorMessage = "Dependency Analysis Gradle Plugin not found.",
    )
    applyPlugins(PLUGIN_ID_DEPENDENCY_ANALYSIS)

    val dependencyAnalysisOptions = createExtension(
      name = DependencyAnalysisOptionsExtension.NAME,
      publicType = DependencyAnalysisOptionsExtension::class,
    )

    configureDependencyAnalysis(dependencyAnalysisOptions)
  }
}

private fun Project.configureDependencyAnalysis(
  dependencyAnalysisOptions: DependencyAnalysisOptionsExtension,
) = extensions.configure<DependencyAnalysisExtension> {
  issues {
    all {
      onAny {
        severity(dependencyAnalysisOptions.severity.get())
      }
    }
  }
}
