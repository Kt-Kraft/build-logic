package convention.android

import convention.common.WithDefaults
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

public abstract class AndroidOptionsExtension @Inject constructor(
  objects: ObjectFactory,
) : WithDefaults<AndroidOptionsExtension> {

  public val minSdk: Property<Int> =
    objects.property(Int::class.java).convention(DEFAULT_MIN_API)

  public val targetSdk: Property<Int> =
    objects.property(Int::class.java).convention(DEFAULT_TARGET_API)

  public val compileSdk: Property<Int> =
    objects.property(Int::class.java).convention(DEFAULT_TARGET_API)

  public val localeFilters: ListProperty<String> =
    objects.listProperty(String::class.java).convention(listOf("en", "id"))

  override fun setDefaults(defaults: AndroidOptionsExtension) {
    minSdk.convention(defaults.minSdk)
    targetSdk.convention(defaults.targetSdk)
    compileSdk.convention(defaults.compileSdk)
    localeFilters.convention(defaults.localeFilters)
  }

  public companion object {
    internal const val NAME = "android"
    internal const val DEFAULT_MIN_API = 28
    internal const val DEFAULT_TARGET_API = 35
  }
}

public val ExtensionContainer.androidOptions: AndroidOptionsExtension
  get() = getByType(AndroidOptionsExtension::class.java)
