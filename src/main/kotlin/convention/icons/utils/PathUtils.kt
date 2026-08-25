package convention.icons.utils

import java.io.File

public object PathUtils {

  public fun resolveCacheDirectory(cacheDirPath: String, projectBuildDir: String): File {
    val cacheDir = File(cacheDirPath)
    return if (cacheDir.isAbsolute) {
      cacheDir
    } else {
      File(projectBuildDir, cacheDirPath)
    }
  }

  public fun isCacheInsideBuildDir(cacheDir: File, buildDir: File): Boolean =
    runCatching {
      val canonicalCache = cacheDir.canonicalFile
      val canonicalBuild = buildDir.canonicalFile
      canonicalCache.startsWith(canonicalBuild)
    }.getOrDefault(false)
}
