package convention.commitlint

import convention.commitlint.githook.GitHook
import convention.commitlint.githook.GitHookWriter
import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import java.io.File
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry

public open class CommitLintPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseConventionPlugin() {

  private val commitLintOptions: CommitLintExtension
    get() = conventionOptions.extensions.commitLintOptions

  @InternalPluginApi
  override fun Project.configure() {
    tasks.register("commitLint") {
      group = "Build Logic"
      description = "Commit Message Verification"

      doLast {
        val msg = File(rootDir, ".git/COMMIT_EDITMSG").readText()
        CommitLintUtil.validate(msg, commitLintOptions.enforceRefs.get())
      }
    }

    afterEvaluate {
      val hooksDir = File(rootDir, ".git/hooks")
      val gradlewFile = File(rootDir, "gradlew")

      if (!hooksDir.exists()) {
        hooksDir.mkdirs()
      }

      val gitHook = GitHook("commit-msg", "commitLint -Dorg.gradle.configuration-cache=false -Dmsgfile=\\\$1")
      GitHookWriter(gradlewFile.absolutePath, hooksDir.absolutePath, gitHook).write()
    }
  }
}
