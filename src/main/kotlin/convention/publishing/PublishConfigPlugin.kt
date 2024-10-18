package convention.publishing

import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import org.gradle.api.Project

public open class PublishConfigPlugin : BaseConventionPlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    createExtension(
      name = PublishingOptionsExtension.NAME,
      publicType = PublishingOptionsExtension::class,
    )
  }
}
