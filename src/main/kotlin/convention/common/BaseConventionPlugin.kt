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

  protected lateinit var conventionOptions: ConventionOptionsExtension
    private set

  @PublishedApi
  @InternalPluginApi
  internal val conventionOptionsExtensions: Sequence<ConventionOptionsExtension>
    get() = mProject.parents.mapNotNull { it.extensions.convention }

  @OptIn(InternalPluginApi::class)
  final override fun apply(target: Project) {
    mProject = target
    conventionOptions = mProject.extensions.obtainConventionOptionsExtension()
    target.configure()
  }

  @InternalPluginApi
  protected abstract fun Project.configure()

  @InternalPluginApi
  protected inline fun <reified T : WithDefaults<T>> createExtension(
    name: String,
    publicType: KClass<in T>? = null,
  ): T {
    val defaults = conventionOptionsExtensions
      .mapNotNull { it.extensions.findExtByName<T>(name) }
      .firstOrNull()
    return (conventionOptions as ExtensionAware).extensions.createWithDefaults(
      name,
      defaults,
      publicType,
    )
  }

  @InternalPluginApi
  private fun ExtensionContainer.obtainConventionOptionsExtension(): ConventionOptionsExtension {
    val conventionOptionsExtensionParent = conventionOptionsExtensions.firstOrNull()
    return when {
      convention != null -> convention!!

      conventionOptionsExtensionParent != null -> {
        add(ConventionOptionsExtension.NAME, conventionOptionsExtensionParent)
        getByType(ConventionOptionsExtension::class.java)
      }

      else -> create(ConventionOptionsExtension.NAME, ConventionOptionsExtension::class.java)
    }
  }
}
