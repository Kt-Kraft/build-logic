package convention.quality

import com.diffplug.gradle.spotless.SpotlessExtension
import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_SPOTLESS
import convention.common.internal.applyPlugins
import convention.common.internal.requiredPlugin
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.kotlin.dsl.configure

public open class SpotlessPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseConventionPlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_SPOTLESS,
      errorMessage = "Spotless Gradle Plugin not found.",
    )
    applyPlugins(PLUGIN_ID_SPOTLESS)

    val spotlessOptions = createExtension(
      name = SpotlessOptionsExtension.NAME,
      publicType = SpotlessOptionsExtension::class,
    )

    configureSpotless(spotlessOptions)
    orderSpotlessAfterClean()
  }
}

private fun Project.configureSpotless(
  spotlessOptions: SpotlessOptionsExtension,
) = extensions.configure<SpotlessExtension> {
  val excludes = spotlessOptions.excludes.get().toTypedArray()
  val ktfmtVersion = spotlessOptions.ktfmtVersion.get()

  kotlin {
    target("**/*.kt")
    targetExclude(*excludes)
    ktfmt(ktfmtVersion).googleStyle()
    trimTrailingWhitespace()
    endWithNewline()
  }

  kotlinGradle {
    target("**/*.gradle.kts")
    targetExclude(*excludes)
    ktfmt(ktfmtVersion).googleStyle()
  }

  format("toml") {
    target("**/*.toml")
    targetExclude(*excludes)
    trimTrailingWhitespace()
    endWithNewline()
  }

  yaml {
    target("**/*.yml", "**/*.yaml")
    targetExclude(*excludes)
    trimTrailingWhitespace()
    endWithNewline()
  }
}

private fun Project.orderSpotlessAfterClean() {
  val cleanTasks = allprojects.map { project -> project.tasks.matching { it.name.contains("clean") } }
  tasks.matching { it.name.startsWith("spotless") }.configureEach {
    cleanTasks.forEach { mustRunAfter(it) }
  }
}
