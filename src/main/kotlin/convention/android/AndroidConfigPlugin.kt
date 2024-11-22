package convention.android

import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import org.gradle.api.Project

public open class AndroidConfigPlugin : BaseConventionPlugin() {
  
  @InternalPluginApi
  override fun Project.configure() {
    createExtension(
      name = AndroidOptionsExtension.NAME,
      publicType = AndroidOptionsExtension::class,
    )
  }
}
