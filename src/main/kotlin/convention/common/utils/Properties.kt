package convention.common.utils

import java.io.File
import java.io.FileNotFoundException
import java.util.Properties

public fun loadPropertiesFile(file: File): Properties {
  if (!file.exists()) {
    throw FileNotFoundException(
      "The file '${file.absolutePath}' could not be found",
    )
  }
  val properties = Properties()
  properties.load(file.inputStream())
  return properties
}
