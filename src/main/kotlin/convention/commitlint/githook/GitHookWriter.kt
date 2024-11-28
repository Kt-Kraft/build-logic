package convention.commitlint.githook

import java.io.File

public class GitHookWriter(
  private val gradlewFile: String,
  private val hooksDir: String,
  private val hook: GitHook,
) {

  public fun write() {
    writeFile(hook)
  }

  private fun writeFile(hook: GitHook) {
    if (!hookFiles.contains(hook.name)) {
      return
    }

    val file = File(hooksDir, hook.name)
    if (isUserCreated(file)) {
      return
    }

    file.writeText(getScript(gradlewFile, hook))
    file.setExecutable(true)
  }

  private fun getScript(gradleCommand: String, hook: GitHook): String {
    return """
        |#!/bin/sh
        |$IDENTIFIER
        |
        |${hook.taskScript(gradleCommand)}
        |
        |${hook.shellScript()}
        |
        |exit 0
        """.trimMargin()
  }

  private fun isUserCreated(file: File): Boolean {
    if (!file.exists()) {
      return false
    }
    return file.readText().indexOf(IDENTIFIER) == -1
  }

  public companion object {
    private const val IDENTIFIER = "# build-logic-githook"
    private val hookFiles = listOf("commit-msg")
  }
}
