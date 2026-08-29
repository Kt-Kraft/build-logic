package convention.commitlint.githook

public data class GitHook(
  val name: String,
  val gradleArgs: List<String> = emptyList(),
  val shell: String? = null,
) {

  public fun script(): String {
    val blocks = buildList {
      add("""ROOT="${D}(git rev-parse --show-toplevel)"""")
      gradleScript()?.let(::add)
      shell?.takeIf { it.isNotBlank() }?.trimEnd()?.let(::add)
      add("exit $D?")
    }
    return blocks.joinToString(
      separator = "\n\n",
      prefix = "#!/bin/sh\n$IDENTIFIER\n\n",
      postfix = "\n",
    )
  }

  private fun gradleScript(): String? {
    if (gradleArgs.isEmpty()) return null
    return gradleArgs.joinToString(
      separator = " \\\n  ",
      prefix = """"${D}ROOT/gradlew" \""" + "\n  ",
    )
  }

  public companion object {
    internal const val IDENTIFIER: String = "# build-logic-githook"

    private const val D: String = "$"
  }
}
