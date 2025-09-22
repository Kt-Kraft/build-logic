package convention.swiftinterop.tasks

import java.io.File

internal fun File.recreateDirectories() {
  deleteRecursively()
  mkdirs()
}
