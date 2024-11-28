package convention.commitlint

import convention.common.WithDefaults
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property

public abstract class CommitLintExtension @Inject constructor(
  objects: ObjectFactory,
) : WithDefaults<CommitLintExtension> {

  public val enforceRefs: Property<Boolean> =
    objects.property(Boolean::class.java).convention(DEFAULT_ENFORCE_REFS)

  override fun setDefaults(defaults: CommitLintExtension) {
    enforceRefs.set(defaults.enforceRefs)
  }

  public companion object {
    internal const val NAME = "commitLint"
    internal const val DEFAULT_ENFORCE_REFS = false
  }
}

public val ExtensionContainer.commitLintOptions: CommitLintExtension
  get() = getByType(CommitLintExtension::class.java)
