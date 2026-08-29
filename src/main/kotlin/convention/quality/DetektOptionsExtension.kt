package convention.quality

import convention.common.WithDefaults
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property

public abstract class DetektOptionsExtension @Inject constructor(
  objects: ObjectFactory,
) : WithDefaults<DetektOptionsExtension> {

  public val buildUponDefaultConfig: Property<Boolean> =
    objects.property(Boolean::class.java).convention(DEFAULT_BUILD_UPON_DEFAULT_CONFIG)

  public val parallel: Property<Boolean> =
    objects.property(Boolean::class.java).convention(DEFAULT_PARALLEL)

  public val autoCorrect: Property<Boolean> =
    objects.property(Boolean::class.java).convention(DEFAULT_AUTO_CORRECT)

  public val configFileName: Property<String> =
    objects.property(String::class.java).convention(DEFAULT_CONFIG_FILE_NAME)

  override fun setDefaults(defaults: DetektOptionsExtension) {
    buildUponDefaultConfig.convention(defaults.buildUponDefaultConfig)
    parallel.convention(defaults.parallel)
    autoCorrect.convention(defaults.autoCorrect)
    configFileName.convention(defaults.configFileName)
  }

  public companion object {
    internal const val NAME: String = "detekt"
    internal const val DEFAULT_BUILD_UPON_DEFAULT_CONFIG: Boolean = true
    internal const val DEFAULT_PARALLEL: Boolean = true
    internal const val DEFAULT_AUTO_CORRECT: Boolean = false
    internal const val DEFAULT_CONFIG_FILE_NAME: String = "detekt/detekt.yml"
  }
}

public val ExtensionContainer.detektOptions: DetektOptionsExtension
  get() = getByType(DetektOptionsExtension::class.java)
