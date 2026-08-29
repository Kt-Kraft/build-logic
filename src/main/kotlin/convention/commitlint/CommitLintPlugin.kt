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
    val repoRoot = rootDir
    val msgFileProperty = providers.systemProperty(MSG_FILE_PROPERTY)
    val enforceRefs = commitLintOptions.enforceRefs

    tasks.register("commitLint") {
      group = "Build Logic"
      description = "Commit Message Verification"

      doLast {
        val msgFile = resolveMessageFile(repoRoot, msgFileProperty.orNull)
        CommitLintUtil.validate(msgFile.readText(), enforceRefs.get())
      }
    }

    installCommitMsgHook(repoRoot)
  }

  private companion object {
    const val MSG_FILE_PROPERTY = "msgfile"
  }
}

private const val DEFAULT_MSG_FILE = ".git/COMMIT_EDITMSG"

private fun resolveMessageFile(repoRoot: File, msgFile: String?): File {
  val path = msgFile?.trim().orEmpty()
  if (path.isEmpty()) {
    return File(repoRoot, DEFAULT_MSG_FILE)
  }
  return File(path).takeIf { it.isAbsolute } ?: File(repoRoot, path)
}

private fun installCommitMsgHook(repoRoot: File) {
  if (!File(repoRoot, ".git").exists()) {
    return
  }

  val hook = GitHook(
    name = "commit-msg",
    gradleArgs = listOf(
      "commitLint",
      "-Dorg.gradle.configuration-cache=false",
      "-Dmsgfile=\"\$1\"",
    ),
  )
  GitHookWriter(File(repoRoot, ".git/hooks"), hook).write()
}
