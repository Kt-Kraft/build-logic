package convention.commitlint

import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import org.gradle.api.Project

public open class CommitLintConfigPlugin : BaseConventionPlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    createExtension(
      name = CommitLintExtension.NAME,
      publicType = CommitLintExtension::class,
    )
  }
}
