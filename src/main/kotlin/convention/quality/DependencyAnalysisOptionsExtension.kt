package convention.quality

import convention.common.WithDefaults
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property

public abstract class DependencyAnalysisOptionsExtension @Inject constructor(
  objects: ObjectFactory,
) : WithDefaults<DependencyAnalysisOptionsExtension> {

  public val severity: Property<String> =
    objects.property(String::class.java).convention(DEFAULT_SEVERITY)

  override fun setDefaults(defaults: DependencyAnalysisOptionsExtension) {
    severity.convention(defaults.severity)
  }

  public companion object {
    internal const val NAME: String = "dependencyAnalysis"
    internal const val DEFAULT_SEVERITY: String = "fail"
  }
}

public val ExtensionContainer.dependencyAnalysisOptions: DependencyAnalysisOptionsExtension
  get() = getByType(DependencyAnalysisOptionsExtension::class.java)
