package convention.publishing

import convention.common.WithDefaults
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPom
import org.gradle.kotlin.dsl.property

public abstract class PublishingOptionsExtension @Inject constructor(
  objects: ObjectFactory,
) : WithDefaults<PublishingOptionsExtension> {

  public val withSource: Property<Boolean> =
    objects.property(Boolean::class.java).convention(false)

  public val configurePom: Property<MavenPom.() -> Unit> =
    objects.property<MavenPom.() -> Unit>().convention {}

  public fun pom(configure: MavenPom.() -> Unit) {
    configurePom.set(configure)
  }

  override fun setDefaults(defaults: PublishingOptionsExtension) {
    withSource.convention(defaults.withSource)
    configurePom.convention(defaults.configurePom)
  }

  public companion object {
    internal const val NAME: String = "publishing"
  }
}

public val ExtensionContainer.publishingOptions: PublishingOptionsExtension
  get() = getByType(PublishingOptionsExtension::class.java)
