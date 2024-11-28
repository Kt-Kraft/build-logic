package convention.commitlint

import org.gradle.api.InvalidUserDataException

internal object CommitLintUtil {

  private val E_INVALID_TYPE = """
    Invalid commit message format. The commit message must start with a valid type
    (build, chore, ci, docs, feat, fix, perf, refactor, revert, style, or test),
    followed by an optional scope in parentheses, an optional '!' for breaking changes,
    and then a description. Example: 'feat(api): add user authentication'.
    See https://www.conventionalcommits.org/en/v1.0.0/
  """.trimIndent()
  private const val E_LONG_SUBJECT = "Commit message exceeds 50 characters."
  private const val E_NO_BLANK_LINE = "Add a blank line before the BODY."
  private const val E_LONG_LINE = "Commit message line exceeds 72 characters."
  private const val E_REFS_REQUIRED = "Commit message should reference an issue in the format 'refs #number'"

  private val SEMVER_REGEX = Regex(
    """^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$"""
  )

  private val IGNORE_PATTERNS = listOf(
    """^(Merge pull request)|(Merge .*? into .*?)|(Merge branch .*?)$""",
    """^(R|r)evert .*""",
    """^(fixup|squash)! .*""",
    """^Merged .*? (in|into) .*""",
    """^Merge remote-tracking branch .*""",
    """^Automatic merge.*""",
    """^Auto-merged .*? into .*"""
  ).map { Regex(it, RegexOption.MULTILINE) }

  private val COMMIT_TYPE_REGEX = Regex(
    """^(build|chore|ci|docs|feat|fix|perf|refactor|revert|style|test)(\([a-z ]+\))?!?: .+$"""
  )

  private fun isSemver(message: String): Boolean {
    val firstLine = message.lineSequence().firstOrNull()?.trim() ?: return false
    val stripped = firstLine
      .removePrefix("chore")
      .replace(Regex("""\([^)]+\):"""), "")
      .trim()
    return SEMVER_REGEX.matches(stripped)
  }

  private fun shouldBeIgnored(message: String): Boolean {
    return isSemver(message) || IGNORE_PATTERNS.any { it.matches(message) }
  }

  internal fun validate(message: String, enforceRefs: Boolean) {
    if (shouldBeIgnored(message)) return

    val lines = message.lineSequence()
      .filterNot { it.trimStart().startsWith("#") }
      .toList()

    require(lines.isNotEmpty()) { E_INVALID_TYPE }

    val firstLine = lines[0]
    validateFirstLine(firstLine)
    validateBodyLines(lines, enforceRefs)
  }

  private fun validateFirstLine(firstLine: String) {
    if (!firstLine.matches(COMMIT_TYPE_REGEX)) {
      throw InvalidUserDataException(E_INVALID_TYPE)
    }

    if (firstLine.length > 50) {
      throw InvalidUserDataException(E_LONG_SUBJECT)
    }
  }

  private fun validateBodyLines(lines: List<String>, enforceRefs: Boolean) {
    if (lines.size > 1 && lines[1].isNotEmpty()) {
      throw InvalidUserDataException(E_NO_BLANK_LINE)
    }

    if (lines.drop(2).any { it.length > 72 }) {
      throw InvalidUserDataException(E_LONG_LINE)
    }

    if (enforceRefs && lines.none { it.contains(Regex("""refs #\d+""")) }) {
      throw InvalidUserDataException(E_REFS_REQUIRED)
    }
  }
}
