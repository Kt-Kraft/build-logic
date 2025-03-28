package convention.multiplatform

import convention.common.WithDefaults
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property

public abstract class MultiplatformOptionsExtension @Inject constructor(
  objects: ObjectFactory,
) : WithDefaults<MultiplatformOptionsExtension> {

  public val jvm: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  public val android: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  public val linux: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  public val iOS: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  public val js: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  public val tvOS: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  public val macOS: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  public val watchOS: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  public val windows: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  public val wasmJs: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  public val wasmWASI: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  public val desktop: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  override fun setDefaults(defaults: MultiplatformOptionsExtension) {
    jvm.convention(defaults.jvm)
    android.convention(defaults.android)
    linux.convention(defaults.linux)
    iOS.convention(defaults.iOS)
    js.convention(defaults.js)
    tvOS.convention(defaults.tvOS)
    macOS.convention(defaults.macOS)
    watchOS.convention(defaults.watchOS)
    windows.convention(defaults.windows)
    wasmJs.convention(defaults.wasmJs)
    wasmWASI.convention(defaults.wasmWASI)
    desktop.convention(defaults.desktop)
  }

  public operator fun component1(): Boolean = jvm.get()
  public operator fun component2(): Boolean = android.get()
  public operator fun component3(): Boolean = linux.get()
  public operator fun component4(): Boolean = iOS.get()
  public operator fun component5(): Boolean = js.get()
  public operator fun component6(): Boolean = tvOS.get()
  public operator fun component7(): Boolean = macOS.get()
  public operator fun component8(): Boolean = watchOS.get()
  public operator fun component9(): Boolean = windows.get()
  public operator fun component10(): Boolean = wasmJs.get()
  public operator fun component11(): Boolean = wasmWASI.get()
  public operator fun component12(): Boolean = desktop.get()

  public companion object {
    internal const val NAME = "multiplatform"
  }
}

public val ExtensionContainer.multiplatformOptions: MultiplatformOptionsExtension
  get() = getByType(MultiplatformOptionsExtension::class.java)
