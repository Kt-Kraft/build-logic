package convention.common

import convention.common.annotation.InternalPluginApi
import convention.common.internal.findExtByName
import convention.common.internal.parents
import kotlin.reflect.KClass
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer

/**
 * This is an abstract class that serves as a base for all convention plugins.
 * It implements the Plugin interface with the Project type parameter.
 *
 * @property mProject The project that the plugin is applied to. It is late-initialized and can only be set privately.
 * @property conventionExtension The convention extension of the project. It is late-initialized and can only be set privately.
 * @property conventionExtensions A sequence of convention extensions from the project's parent projects.
 *
 * @constructor Creates a new instance of the BaseConventionPlugin class.
 */
public abstract class BaseConventionPlugin : Plugin<Project> {

  /**
   * The project that the plugin is applied to.
   */
  @InternalPluginApi
  public lateinit var mProject: Project
    private set

  /**
   * The convention extension of the project.
   */
  protected lateinit var conventionExtension: ConventionExtension
    private set

  /**
   * A sequence of convention extensions from the project's parent projects.
   */
  @PublishedApi
  @InternalPluginApi
  internal val conventionExtensions: Sequence<ConventionExtension>
    get() = mProject.parents.mapNotNull { it.extensions.convention }

  /**
   * Applies the plugin to the target project.
   * This method is final and cannot be overridden.
   *
   * @param target The project to apply the plugin to.
   */
  @OptIn(InternalPluginApi::class)
  final override fun apply(target: Project) {
    mProject = target
    conventionExtension = mProject.extensions.obtainConventionExtension()
    target.configure()
  }

  /**
   * Configures the project.
   * This method is abstract and must be implemented by subclasses.
   */
  @InternalPluginApi
  protected abstract fun Project.configure()

  /**
   * Creates an extension with the specified name and public type.
   * The extension is created with defaults from the convention extensions of the project's parent projects.
   *
   * @param name The name of the extension.
   * @param publicType The public type of the extension. If null, the type is inferred from the type parameter T.
   * @return The created extension.
   */
  @InternalPluginApi
  protected inline fun <reified T : WithDefaults<T>> createExtension(
    name: String,
    publicType: KClass<in T>? = null,
  ): T {
    val defaults = conventionExtensions
      .mapNotNull { it.extensions.findExtByName<T>(name) }
      .firstOrNull()
    return (conventionExtension as ExtensionAware).extensions.createWithDefaults(
      name,
      defaults,
      publicType,
    )
  }

  /**
   * Obtains the convention extension from the extension container.
   * If the convention extension does not exist, it is created.
   *
   * @return The convention extension.
   */
  @InternalPluginApi
  private fun ExtensionContainer.obtainConventionExtension(): ConventionExtension {
    val conventionExtensionParent = conventionExtensions.firstOrNull()
    return when {
      convention != null -> convention!!

      conventionExtensionParent != null -> {
        add(ConventionExtension.NAME, conventionExtensionParent)
        getByType(ConventionExtension::class.java)
      }

      else -> create(ConventionExtension.NAME, ConventionExtension::class.java)
    }
  }
}
