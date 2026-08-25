package convention.icons.taks.internal

import convention.icons.model.IconConfig

internal data class DownloadStats(
  val totalCount: Int,
  val successCount: Int,
  val failedCount: Int,
  val cachedCount: Int,
  val results: List<DownloadResult>,
)

internal sealed class DownloadResult {
  data class Success(val iconName: String, val config: IconConfig, val fileName: String) :
    DownloadResult()

  data class Failed(val iconName: String, val config: IconConfig, val error: String) :
    DownloadResult()
}
