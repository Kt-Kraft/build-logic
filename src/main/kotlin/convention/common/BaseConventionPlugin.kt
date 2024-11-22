package convention.common

import convention.common.annotation.InternalPluginApi
import convention.common.internal.findExtByName
import convention.common.internal.parents
import kotlin.reflect.KClass
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer

public abstract class BaseConventionPlugin : Plugin<Project> {

  @InternalPluginApi
  public lateinit var mProject: Project
    private set

  protected lateinit var conventionExtension: ConventionExtension
    private set

  @PublishedApi
  @InternalPluginApi
  internal val conventionExtensions: Sequence<ConventionExtension>
    get() = mProject.parents.mapNotNull { it.extensions.convention }

  @OptIn(InternalPluginApi::class)
  final override fun apply(target: Project) {
    mProject = target
    conventionExtension = mProject.extensions.obtainConventionExtension()
    target.configure()
  }

  @InternalPluginApi
  protected abstract fun Project.configure()

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
