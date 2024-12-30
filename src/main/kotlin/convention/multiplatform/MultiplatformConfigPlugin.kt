package convention.multiplatform

import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import org.gradle.api.Project

public open class MultiplatformConfigPlugin : BaseConventionPlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    createExtension(
      name = MultiplatformOptionsExtension.NAME,
      publicType = MultiplatformOptionsExtension::class,
    )
  }
}
