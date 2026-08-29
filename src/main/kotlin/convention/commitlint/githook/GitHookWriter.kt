package convention.commitlint.githook

import java.io.File

public class GitHookWriter(
  private val hooksDir: File,
  private val hook: GitHook,
) {

  public fun write() {
    if (hook.name !in SUPPORTED_HOOKS) {
      return
    }

    val file = File(hooksDir, hook.name)
    if (isUserCreated(file)) {
      return
    }

    val script = hook.script()
    if (file.exists() && file.readText() == script) {
      file.ensureExecutable()
      return
    }

    hooksDir.mkdirs()
    file.writeText(script)
    file.ensureExecutable()
  }

  private fun File.ensureExecutable() {
    if (!canExecute()) {
      setExecutable(true)
    }
  }

  private fun isUserCreated(file: File): Boolean {
    if (!file.exists()) {
      return false
    }
    return !file.readText().contains(GitHook.IDENTIFIER)
  }

  public companion object {
    private val SUPPORTED_HOOKS = setOf("commit-msg")
  }
}
