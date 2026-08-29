package convention.quality

import convention.common.WithDefaults
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

public abstract class SpotlessOptionsExtension @Inject constructor(
  objects: ObjectFactory,
) : WithDefaults<SpotlessOptionsExtension> {

  public val ktfmtVersion: Property<String> =
    objects.property(String::class.java).convention(DEFAULT_KTFMT_VERSION)

  public val excludes: ListProperty<String> =
    objects.listProperty(String::class.java).convention(DEFAULT_EXCLUDES)

  override fun setDefaults(defaults: SpotlessOptionsExtension) {
    ktfmtVersion.convention(defaults.ktfmtVersion)
    excludes.convention(defaults.excludes)
  }

  public companion object {
    internal const val NAME: String = "spotless"
    internal const val DEFAULT_KTFMT_VERSION: String = "0.64"

    internal val DEFAULT_EXCLUDES: List<String> = listOf(
      "**/build/**",
      "**/.gradle/**",
      "**/generated/**",
    )
  }
}

public val ExtensionContainer.spotlessOptions: SpotlessOptionsExtension
  get() = getByType(SpotlessOptionsExtension::class.java)
