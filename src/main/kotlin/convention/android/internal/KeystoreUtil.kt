package convention.android.internal

import convention.common.utils.loadPropertiesFile
import java.util.Properties
import org.gradle.api.Project

internal data class KeystoreConfig(
  val keyAlias: String,
  val keyPassword: String,
  val storeFile: String,
  val storePassword: String
)

internal fun Properties.getRequiredProperty(propertyName: String): String {
  return getProperty(propertyName) ?: error("$propertyName is required in keystore.properties")
}

internal fun Properties.loadKeystoreConfig(prefix: String): KeystoreConfig {
  return KeystoreConfig(
    keyAlias = getRequiredProperty("${prefix}_KEY_ALIAS"),
    keyPassword = getRequiredProperty("${prefix}_KEY_PASSWORD"),
    storeFile = getRequiredProperty("${prefix}_STORE_FILE"),
    storePassword = getRequiredProperty("${prefix}_STORE_PASSWORD")
  )
}

internal fun Project.loadKeystoreProperties(
  primary: String = "keystore.properties",
  fallback: String = "keystore.defaults.properties"
): Properties {
  val primaryFile = rootDir.resolve(primary)
  val fallbackFile = rootDir.resolve(fallback)

  return runCatching {
    loadPropertiesFile(primaryFile)
  }.getOrElse {
    loadPropertiesFile(fallbackFile)
  }
}
