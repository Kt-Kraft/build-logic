package convention.common

import convention.common.internal.findExtByName
import javax.inject.Inject
import org.gradle.api.JavaVersion
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

public abstract class ConventionOptionsExtension @Inject constructor(
  layout: ProjectLayout,
  objects: ObjectFactory,
) : ExtensionAware, WithDefaults<ConventionOptionsExtension> {

  public val javaVersion: Property<JavaVersion> =
    objects.property(JavaVersion::class.java).convention(JavaVersion.VERSION_17)

  public val jvmTarget: Property<JvmTarget> =
    objects.property(JvmTarget::class.java).convention(JvmTarget.JVM_17)

  public val jvmToolchainVersion: Property<Int> =
    objects.property(Int::class.java).convention(21)

  public val configsDir: DirectoryProperty =
    objects.directoryProperty().convention(layout.projectDirectory.dir(DEFAULT_CONFIGS_DIR))

  public val reportsDir: DirectoryProperty =
    objects.directoryProperty().convention(layout.buildDirectory.dir(DEFAULT_REPORTS_DIR))

  override fun setDefaults(defaults: ConventionOptionsExtension) {
    javaVersion.convention(defaults.javaVersion)
    jvmTarget.convention(defaults.jvmTarget)
    jvmToolchainVersion.convention(defaults.jvmToolchainVersion)
    configsDir.convention(defaults.configsDir)
    reportsDir.convention(defaults.reportsDir)
  }

  public companion object {
    internal const val NAME: String = "convention"
    internal const val DEFAULT_CONFIGS_DIR = "config/"
    internal const val DEFAULT_REPORTS_DIR = "reports/"
  }
}

public val ExtensionContainer.convention: ConventionOptionsExtension?
  get() = findExtByName(ConventionOptionsExtension.NAME)
