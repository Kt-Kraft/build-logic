package convention.swiftinterop

import convention.common.WithDefaults
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property

public abstract class SwiftInteropExtension @Inject constructor(
  objects: ObjectFactory
) : WithDefaults<SwiftInteropExtension> {

  public val packageName: Property<String> =
    objects.property(String::class.java)

  public val swiftToolsVersion: Property<String> =
    objects.property(String::class.java).convention("5.10")

  // TODO: make defaults equal to K/N values ?
  // TODO: rename to min*
  public val iosVersion: Property<String> =
    objects.property(String::class.java)

  public val macosVersion: Property<String> =
    objects.property(String::class.java)

  public val tvosVersion: Property<String> =
    objects.property(String::class.java)

  public val watchosVersion: Property<String> =
    objects.property(String::class.java)

  internal val swiftInteropModuleName = packageName.map { it.replace(".", "_") }

  override fun setDefaults(defaults: SwiftInteropExtension) {
    iosVersion.convention(defaults.iosVersion)
    macosVersion.convention(defaults.macosVersion)
    tvosVersion.convention(defaults.tvosVersion)
    watchosVersion.convention(defaults.watchosVersion)
  }

  public companion object {
    internal const val NAME = "swiftInterop"
  }
}

public val ExtensionContainer.swiftInteropOptions: SwiftInteropExtension
  get() = getByType(SwiftInteropExtension::class.java)
