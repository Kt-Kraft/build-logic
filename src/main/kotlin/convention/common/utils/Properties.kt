package convention.common.utils

import java.io.FileNotFoundException
import java.util.Properties
import org.gradle.api.Project

/**
 * This function is an extension function for the Project class.
 * It is used to load a properties file from the project directory.
 *
 * The function takes the name of the properties file as an argument.
 * It then creates a File object for the properties file and checks if the file exists.
 * If the file does not exist, it throws a FileNotFoundException with a message indicating the absolute path of the missing file.
 * If the file exists, it creates a Properties object, loads the properties from the file into the Properties object, and returns the Properties object.
 *
 * @param fileName The name of the properties file to load.
 * @return A Properties object containing the properties loaded from the file.
 * @throws FileNotFoundException If the properties file could not be found.
 */
public fun Project.loadPropertiesFile(fileName: String): Properties {
  val propertiesFile = file(fileName)
  if (!propertiesFile.exists()) {
    throw FileNotFoundException(
      "The file '${propertiesFile.absolutePath}' could not be found",
    )
  }
  val properties = Properties()
  properties.load(propertiesFile.inputStream())
  return properties
}
