package convention.swiftinterop

import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import org.gradle.api.Project

public open class SwiftInteropConfigPlugin : BaseConventionPlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    createExtension(
      name = SwiftInteropExtension.NAME,
      publicType = SwiftInteropExtension::class,
    )
  }
}
