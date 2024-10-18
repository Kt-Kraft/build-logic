package convention.android

import convention.common.WithDefaults
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property

/**
 * This abstract class represents the Android options extension.
 * It extends the WithDefaults interface and is used to set default values for Android options.
 *
 * @property objects The object factory used to create properties. This is injected in the constructor.
 * @constructor Creates an instance of the AndroidOptionsExtension class.
 */
public abstract class AndroidOptionsExtension @Inject constructor(
  objects: ObjectFactory,
) : WithDefaults<AndroidOptionsExtension> {

  /**
   * The minimum SDK version for the Android project.
   * It is a property of type Int and its default value is set to DEFAULT_MIN_API.
   */
  public val minSdk: Property<Int> =
    objects.property(Int::class.java).convention(DEFAULT_MIN_API)

  /**
   * The target SDK version for the Android project.
   * It is a property of type Int and its default value is set to DEFAULT_TARGET_API.
   */
  public val targetSdk: Property<Int> =
    objects.property(Int::class.java).convention(DEFAULT_TARGET_API)

  /**
   * The compile SDK version for the Android project.
   * It is a property of type Int and its default value is set to DEFAULT_TARGET_API.
   */
  public val compileSdk: Property<Int> =
    objects.property(Int::class.java).convention(DEFAULT_TARGET_API)

  /**
   * This function is used to set the default values for the Android options.
   * It sets the default values for minSdk, targetSdk, and compileSdk.
   *
   * @param defaults The default values for the Android options.
   */
  override fun setDefaults(defaults: AndroidOptionsExtension) {
    minSdk.convention(defaults.minSdk)
    targetSdk.convention(defaults.targetSdk)
    compileSdk.convention(defaults.compileSdk)
  }

  public companion object {
    internal const val NAME = "android"
    internal const val DEFAULT_MIN_API = 26
    internal const val DEFAULT_TARGET_API = 34
  }
}

/**
 * This is an extension property for the ExtensionContainer class.
 * It provides a convenient way to access the AndroidOptionsExtension instance from the ExtensionContainer.
 *
 * @return The AndroidOptionsExtension instance from the ExtensionContainer.
 */
public val ExtensionContainer.androidOptions: AndroidOptionsExtension
  get() = getByType(AndroidOptionsExtension::class.java)
