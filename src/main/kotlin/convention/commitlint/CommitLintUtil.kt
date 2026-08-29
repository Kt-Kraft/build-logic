package convention.commitlint

import org.gradle.api.InvalidUserDataException

internal object CommitLintUtil {

  private const val MAX_HEADER_LENGTH = 50
  private const val MAX_BODY_LINE_LENGTH = 72

  private const val SCISSORS = "# ------------------------ >8 ------------------------"

  private val TYPES = listOf(
    "build", "chore", "ci", "docs", "feat", "fix",
    "perf", "refactor", "revert", "style", "test",
  )

  private val E_EMPTY_MESSAGE = "Commit message is empty."
  private val E_INVALID_HEADER = """
    Invalid commit message format. The commit message must start with a valid type
    (${TYPES.joinToString()}),
    followed by an optional scope in parentheses, an optional '!' for breaking changes,
    and then a description. Example: 'feat(api): add user authentication'.
    See https://www.conventionalcommits.org/en/v1.0.0/
  """.trimIndent()
  private const val E_LONG_HEADER = "Subject line exceeds $MAX_HEADER_LENGTH characters."
  private const val E_SUBJECT_CASE = "Subject must not start with an upper case letter."
  private const val E_SUBJECT_FULL_STOP = "Subject must not end with a period."
  private const val E_NO_BLANK_LINE = "Add a blank line before the BODY."
  private const val E_LONG_LINE = "Body line exceeds $MAX_BODY_LINE_LENGTH characters."
  private const val E_REFS_REQUIRED =
    "Commit message should reference an issue in the format 'refs #number'"

  private val HEADER_REGEX = Regex(
    """^(${TYPES.joinToString("|")})(\([a-z0-9][a-z0-9._/-]*(?:,\s?[a-z0-9][a-z0-9._/-]*)*\))?!?: (.+)$"""
  )

  private val SEMVER_REGEX = Regex(
    """^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$"""
  )

  private val RELEASE_PREFIX_REGEX = Regex("""^(chore|release)?(\([^)]+\))?:?\s*""")

  private val REFS_REGEX = Regex("""refs #\d+""")

  private val IGNORE_PATTERNS = listOf(
    """^(Merge pull request)|(Merge .*? into .*?)|(Merge branch .*?)$""",
    """^(R|r)evert .*""",
    """^(fixup|squash)! .*""",
    """^Merged .*? (in|into) .*""",
    """^Merge remote-tracking branch .*""",
    """^Automatic merge.*""",
    """^Auto-merged .*? into .*""",
  ).map { Regex(it) }

  internal fun validate(message: String, enforceRefs: Boolean) {
    val lines = contentLines(message)

    if (lines.isEmpty() || lines.all { it.isBlank() }) {
      throw InvalidUserDataException(E_EMPTY_MESSAGE)
    }

    val header = lines.first().trimEnd()
    if (shouldBeIgnored(header)) return

    val errors = buildList {
      addAll(validateHeader(header))
      addAll(validateBody(lines))
      if (enforceRefs && lines.none { REFS_REGEX.containsMatchIn(it) }) {
        add(E_REFS_REQUIRED)
      }
    }

    if (errors.isNotEmpty()) {
      throw InvalidUserDataException(errors.joinToString(separator = "\n\n"))
    }
  }

  private fun contentLines(message: String): List<String> {
    val body = message.substringBefore(SCISSORS)
    return body.lineSequence()
      .filterNot { it.startsWith("#") }
      .toList()
      .dropLastWhile { it.isBlank() }
  }

  private fun validateHeader(header: String): List<String> {
    val match = HEADER_REGEX.matchEntire(header)
      ?: return listOf(E_INVALID_HEADER)

    val subject = match.groupValues[3].trim()
    return buildList {
      if (header.length > MAX_HEADER_LENGTH) add(E_LONG_HEADER)
      if (subject.first().isUpperCase()) add(E_SUBJECT_CASE)
      if (subject.endsWith(".")) add(E_SUBJECT_FULL_STOP)
    }
  }

  private fun validateBody(lines: List<String>): List<String> {
    if (lines.size < 2) return emptyList()

    if (lines[1].isNotBlank()) return listOf(E_NO_BLANK_LINE)

    val hasLongLine = lines.asSequence()
      .drop(2)
      .map { it.trimEnd() }
      .any { it.length > MAX_BODY_LINE_LENGTH && it.isWrappable() }

    return if (hasLongLine) listOf(E_LONG_LINE) else emptyList()
  }

  private fun String.isWrappable(): Boolean {
    return split(' ').none { it.length > MAX_BODY_LINE_LENGTH }
  }

  private fun shouldBeIgnored(header: String): Boolean {
    return isSemver(header) || IGNORE_PATTERNS.any { it.matches(header) }
  }

  private fun isSemver(header: String): Boolean {
    return SEMVER_REGEX.matches(header.replaceFirst(RELEASE_PREFIX_REGEX, "").trim())
  }
}
