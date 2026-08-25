package convention.icons.download

import convention.icons.model.IconConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.readText
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

public class SvgDownloader(
  cacheDirectory: String,
  private val cacheEnabled: Boolean = true,
  private val maxRetries: Int = 3,
  private val retryDelayMs: Long = 1000L,
  private val logger: ((String) -> Unit)? = null,
) {

  public companion object {
    private const val REQUEST_TIMEOUT_MS = 30_000L
    private const val CACHE_MAX_AGE_DAYS = 7
    private val CACHE_MAX_AGE_MS = CACHE_MAX_AGE_DAYS.days.inWholeMilliseconds

    private const val MAX_SVG_SIZE = 10 * 1024 * 1024

    public const val MAX_CONNECTIONS_COUNT: Int = 50
    public const val MAX_CONNECTIONS_PER_ROUTE: Int = 20
  }

  private val httpClient = HttpClient(CIO) {
    engine {
      requestTimeout = REQUEST_TIMEOUT_MS
      maxConnectionsCount = MAX_CONNECTIONS_COUNT
      endpoint { maxConnectionsPerRoute = MAX_CONNECTIONS_PER_ROUTE }
    }
  }

  private fun log(message: String) = logger?.invoke(message) ?: println(message)

  private val cachePath = Path(cacheDirectory).also { path ->
    if (cacheEnabled) path.createDirectories()
  }

  public suspend fun downloadSvg(iconName: String, config: IconConfig): String? =
    withContext(Dispatchers.IO) {
      val cacheKey = config.getCacheKey(iconName)

      if (cacheEnabled) {
        getCachedSvg(cacheKey)?.let { return@withContext it }
      }

      val url = config.buildUrl(iconName)
      var lastError: Exception? = null

      repeat(maxRetries) { attempt ->
        try {
          val result = downloadSvgInternal(url, cacheKey)
          if (attempt > 0) log("Successfully downloaded after ${attempt + 1} attempt(s): $url")
          return@withContext result
        } catch (e: Exception) {
          lastError = e
          val retriesLeft = maxRetries - attempt - 1
          if (retriesLeft > 0) {
            val delayMs = retryDelayMs * (1 shl attempt)
            log("Attempt ${attempt + 1} failed for $url: ${e.message}")
            log("Retrying in ${delayMs}ms... ($retriesLeft retries left)")
            delay(delayMs)
          }
        }
      }

      log("Error downloading SVG from $url after $maxRetries attempts: ${lastError?.message}")
      null
    }

  private suspend fun downloadSvgInternal(url: String, cacheKey: String): String {
    log("Downloading SVG from $url")

    require(url.startsWith("https://")) {
      "Only HTTPS URLs are allowed for security. Got: $url"
    }

    val response = httpClient.get(url)
    if (!response.status.isSuccess()) {
      throw kotlinx.io.IOException(
        "Failed to download from $url: HTTP ${response.status.value} ${response.status.description}"
      )
    }

    val contentType = response.contentType()
    require(
      contentType?.match(ContentType.Text.Xml) == true ||
        contentType?.match(ContentType.Image.SVG) == true
    ) { "Invalid content type: $contentType for URL: $url" }

    val contentLength = response.contentLength()
    if (contentLength != null && contentLength > MAX_SVG_SIZE) {
      throw IllegalStateException(
        "SVG too large: $contentLength bytes (max: $MAX_SVG_SIZE) from URL: $url"
      )
    }

    val svgContent =
      if (contentLength == null) readStreamingSvg(response.body(), url)
      else response.bodyAsText()

    require("<svg" in svgContent && "</svg>" in svgContent) {
      "Invalid SVG structure (missing svg tags) from URL: $url"
    }

    // Security validation: Prevent XXE and other attacks
    validateSvgSecurity(svgContent, url)

    if (cacheEnabled && svgContent.isNotBlank()) {
      cacheSvg(cacheKey, svgContent, url)
    }

    return svgContent
  }

  private suspend fun readStreamingSvg(channel: ByteReadChannel, url: String): String {
    val packet = channel.readRemaining(MAX_SVG_SIZE.toLong() + 1)
    return try {
      if (packet.remaining > MAX_SVG_SIZE || !channel.isClosedForRead) {
        throw IllegalStateException(
          "SVG response exceeds max size of $MAX_SVG_SIZE bytes from URL: $url"
        )
      }
      packet.readText()
    } finally {
      packet.close()
    }
  }

  private fun validateSvgSecurity(svgContent: String, url: String) {
    // List of dangerous patterns that should never appear in safe SVG files
    // Using regex to prevent whitespace-based bypass attacks (e.g., "< script" instead of
    // "<script")
    val dangerousPatterns =
      mapOf(
        Regex("<!\\s*ENTITY", RegexOption.IGNORE_CASE) to
          "XML External Entity (XXE) declaration",
        Regex("<!\\s*DOCTYPE", RegexOption.IGNORE_CASE) to
          "DOCTYPE declaration (potential XXE vector)",
        Regex("<\\s*script", RegexOption.IGNORE_CASE) to "Embedded JavaScript",
        Regex("javascript:", RegexOption.IGNORE_CASE) to "JavaScript protocol handler",
        Regex("data:text/html", RegexOption.IGNORE_CASE) to "HTML data URL",
        Regex("on\\w+\\s*=", RegexOption.IGNORE_CASE) to "Event handler attribute",
        Regex("<\\s*iframe", RegexOption.IGNORE_CASE) to "Embedded iframe",
        Regex("<\\s*object", RegexOption.IGNORE_CASE) to "Embedded object",
        Regex("<\\s*embed", RegexOption.IGNORE_CASE) to "Embedded content",
        Regex("xlink:href\\s*=\\s*\"?javascript:", RegexOption.IGNORE_CASE) to
          "XLink JavaScript protocol",
      )

    dangerousPatterns.forEach { (pattern, description) ->
      if (pattern.containsMatchIn(svgContent)) {
        throw SecurityException(
          "SVG contains potentially dangerous content from $url: $description (pattern: '${pattern.pattern}'). " +
            "This file may be malicious and has been rejected for security reasons."
        )
      }
    }

    // Additional check: Ensure no SYSTEM or PUBLIC entities
    val systemEntityRegex = Regex("SYSTEM", RegexOption.IGNORE_CASE)
    val publicEntityRegex = Regex("PUBLIC", RegexOption.IGNORE_CASE)
    val entityDeclRegex = Regex("<!\\s*ENTITY", RegexOption.IGNORE_CASE)
    val doctypeDeclRegex = Regex("<!\\s*DOCTYPE", RegexOption.IGNORE_CASE)

    if (
      systemEntityRegex.containsMatchIn(svgContent) &&
      (entityDeclRegex.containsMatchIn(svgContent) ||
        doctypeDeclRegex.containsMatchIn(svgContent))
    ) {
      throw SecurityException(
        "SVG contains SYSTEM entity declaration from $url. " +
          "This is a critical security risk (potential file disclosure) and has been rejected."
      )
    }

    if (
      publicEntityRegex.containsMatchIn(svgContent) &&
      (entityDeclRegex.containsMatchIn(svgContent) ||
        doctypeDeclRegex.containsMatchIn(svgContent))
    ) {
      throw SecurityException(
        "SVG contains PUBLIC entity declaration from $url. " +
          "This is a security risk and has been rejected."
      )
    }
  }

  private fun getCachedSvg(cacheKey: String): String? {
    val cacheFile = cachePath / "$cacheKey.svg"
    val metaFile = cachePath / "$cacheKey.meta"
    if (!cacheFile.exists() || !metaFile.exists()) return null

    return runCatching {
      val meta = metaFile.readLines()
      if (meta.size < 2) return@runCatching null

      val timestamp = meta[0].toLong()
      if (System.currentTimeMillis() - timestamp < CACHE_MAX_AGE_MS) {
        cacheFile.readText()
      } else null
    }.getOrNull()
  }

  private fun cacheSvg(cacheKey: String, content: String, url: String) {
    val cacheFile = cachePath / "$cacheKey.svg"
    val metaFile = cachePath / "$cacheKey.meta"

    runCatching {
      cacheFile.writeText(content)
      metaFile.writeText(
        buildString {
          appendLine(System.currentTimeMillis())
          appendLine(url)
          append(calculateSHA256(content))
        }
      )
    }.onFailure { e ->
      log("Failed to cache SVG for key $cacheKey: ${e.message}")
    }
  }

  private fun calculateSHA256(content: String): String =
    MessageDigest.getInstance("SHA-256")
      .digest(content.toByteArray())
      .joinToString("") { "%02x".format(it) }

  public fun cleanup(): Unit = httpClient.close()

  public fun isCached(cacheKey: String): Boolean {
    if (!cacheEnabled) return false

    val metaFile = cachePath / "$cacheKey.meta"
    return runCatching {
      val timestamp = metaFile.readLines().first().toLong()
      System.currentTimeMillis() - timestamp < CACHE_MAX_AGE_MS
    }.getOrDefault(false)
  }

  public fun getCacheStats(): CacheStats {
    if (!cacheEnabled || !cachePath.exists()) return CacheStats(0, 0)

    val svgFiles = cachePath.listDirectoryEntries("*.svg")
    val totalSize = svgFiles.sumOf { it.fileSize() }

    return CacheStats(svgFiles.size, totalSize)
  }

  public data class CacheStats(
    val fileCount: Int,
    val totalSizeBytes: Long
  ) {
    val totalSizeKB: Double get() = totalSizeBytes / 1024.0
    val totalSizeMB: Double get() = totalSizeKB / 1024.0
  }
}
