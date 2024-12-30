package convention.commitlint

import convention.commitlint.githook.GitHook
import convention.commitlint.githook.GitHookWriter
import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import java.io.File
import org.gradle.api.Project

public open class CommitLintPlugin : BaseConventionPlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    val extension = createExtension(
      name = CommitLintExtension.NAME,
      publicType = CommitLintExtension::class,
    )

    tasks.register("commitLint") {
      group = "Build Logic"
      description = "Commit Message Verification"

      doLast {
        val msg = File(rootDir, ".git/COMMIT_EDITMSG").readText()
        CommitLintUtil.validate(msg, extension.enforceRefs.get())
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
