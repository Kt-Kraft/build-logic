package convention.compose

import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import convention.compose.internal.configureCompose
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty

public abstract class BaseComposePlugin : BaseConventionPlugin() {

  private val reportsDir: DirectoryProperty
    get() = conventionOptions.reportsDir

  @InternalPluginApi
  protected fun Project.configureCommonComposeAndroid() {
    configureCompose(reportsDir = reportsDir)
  }
}
