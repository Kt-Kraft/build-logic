package convention.commitlint.githook

public data class GitHook(
  val name: String,
  var task: String? = null,
  var shell: String? = null
) {

  public fun taskScript(gradleCommand: String): String {
    return task?.takeIf { it.isNotBlank() }?.let {
      """
      |$(echo "$gradleCommand") $it
      |$CHECK_EXIT_STATUS
      """.trimMargin()
    }.orEmpty()
  }

  public fun shellScript(): String {
    return shell?.takeIf { it.isNotBlank() }?.let {
      """
      |$it
      |$CHECK_EXIT_STATUS
      """.trimMargin()
    }.orEmpty()
  }

  public companion object {
    private const val CHECK_EXIT_STATUS = "[ \$? -gt 0 ] && exit 1"
  }
}
