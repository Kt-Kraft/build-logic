package convention.common

import convention.common.internal.findExtByName
import javax.inject.Inject
import org.gradle.api.JavaVersion
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

public abstract class ConventionOptionsExtension @Inject constructor(
  layout: ProjectLayout,
  objects: ObjectFactory,
) : ExtensionAware, WithDefaults<ConventionOptionsExtension> {

  public val jvmToolchainVersion: Property<Int> =
    objects.property(Int::class.java).convention(DEFAULT_JVM_TOOLCHAIN_VERSION)

  public val jvmTarget: Property<JvmTarget> =
    objects.property(JvmTarget::class.java).convention(DEFAULT_JVM_TARGET)

  public val javaVersion: Provider<JavaVersion> =
    jvmTarget.map { JavaVersion.toVersion(it.target) }

  internal val jvmToolchainLanguageVersion: Provider<JavaLanguageVersion> =
    jvmToolchainVersion.zip(jvmTarget) { toolchain, target ->
      val targetVersion = target.target.substringAfterLast('.').toInt()
      require(toolchain >= targetVersion) {
        "jvmToolchainVersion ($toolchain) must not be lower than jvmTarget (${target.target})."
      }
      JavaLanguageVersion.of(toolchain)
    }

  public val optIns: ListProperty<String> =
    objects.listProperty(String::class.java).convention(DEFAULT_OPT_INS)

  public val freeCompilerArgs: ListProperty<String> =
    objects.listProperty(String::class.java).convention(DEFAULT_FREE_COMPILER_ARGS)

  public val configsDir: DirectoryProperty =
    objects.directoryProperty().convention(layout.projectDirectory.dir(DEFAULT_CONFIGS_DIR))

  public val reportsDir: DirectoryProperty =
    objects.directoryProperty().convention(layout.buildDirectory.dir(DEFAULT_REPORTS_DIR))

  override fun setDefaults(defaults: ConventionOptionsExtension) {
    jvmToolchainVersion.convention(defaults.jvmToolchainVersion)
    jvmTarget.convention(defaults.jvmTarget)
    optIns.convention(defaults.optIns)
    freeCompilerArgs.convention(defaults.freeCompilerArgs)
    configsDir.convention(defaults.configsDir)
    reportsDir.convention(defaults.reportsDir)
  }

  public companion object {
    internal const val NAME: String = "convention"
    internal const val DEFAULT_CONFIGS_DIR = "config/"
    internal const val DEFAULT_REPORTS_DIR = "reports/"
    internal const val DEFAULT_JVM_TOOLCHAIN_VERSION: Int = 21
    internal val DEFAULT_JVM_TARGET: JvmTarget = JvmTarget.JVM_17

    internal val DEFAULT_OPT_INS: List<String> = listOf(
      "kotlin.experimental.ExperimentalTypeInference",
      "kotlin.uuid.ExperimentalUuidApi",
      "kotlin.contracts.ExperimentalContracts",
    )

    internal val DEFAULT_FREE_COMPILER_ARGS: List<String> = listOf(
      "-Xexpect-actual-classes",
      "-Xconsistent-data-class-copy-visibility",
      "-Xwarning-level=NOTHING_TO_INLINE:disabled",
      "-Xwarning-level=UNUSED_ANONYMOUS_PARAMETER:disabled",
    )
  }
}

public val ExtensionContainer.convention: ConventionOptionsExtension?
  get() = findExtByName(ConventionOptionsExtension.NAME)
