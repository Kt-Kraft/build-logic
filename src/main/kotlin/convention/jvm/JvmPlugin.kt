package convention.jvm

import convention.common.BaseConventionPlugin
import convention.common.ConventionOptionsExtension
import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_KOTLIN_JVM
import convention.common.internal.applyPlugins
import convention.common.internal.requiredPlugin
import convention.common.utils.addDistinctCompilerArgs
import convention.common.utils.addDistinctOptIns
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

public open class JvmPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseConventionPlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_KOTLIN_JVM,
      errorMessage = "Kotlin JVM Gradle Plugin not found.",
    )
    applyPlugins(PLUGIN_ID_KOTLIN_JVM)

    configureKotlinJvm(conventionOptions)
    configureJava(conventionOptions)
  }
}

private fun Project.configureKotlinJvm(
  conventionOptions: ConventionOptionsExtension,
) = extensions.configure<KotlinJvmProjectExtension> {
  explicitApi()

  compilerOptions {
    addDistinctCompilerArgs(conventionOptions.freeCompilerArgs.get())
    addDistinctOptIns(conventionOptions.optIns.get())
    progressiveMode.set(true)
    jvmTarget.set(conventionOptions.jvmTarget)
  }

  jvmToolchain {
    languageVersion.set(conventionOptions.jvmToolchainLanguageVersion)
  }
}

private fun Project.configureJava(
  conventionOptions: ConventionOptionsExtension,
) {
  extensions.configure<JavaPluginExtension> {
    toolchain.languageVersion.set(conventionOptions.jvmToolchainLanguageVersion)
  }

  tasks.withType<JavaCompile>().configureEach {
    options.release.set(conventionOptions.javaVersion.map { it.majorVersion.toInt() })
  }
}
