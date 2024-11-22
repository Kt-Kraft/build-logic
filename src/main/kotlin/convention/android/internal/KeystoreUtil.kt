package convention.android.internal

import java.util.Properties

internal data class KeystoreConfig(
  val keyAlias: String,
  val keyPassword: String,
  val storeFile: String,
  val storePassword: String
)

internal fun Properties.loadKeystoreConfig(prefix: String): KeystoreConfig {
  fun getRequiredProperty(propertyName: String): String {
    return getProperty(propertyName) ?: error("$propertyName is required in keystore.properties")
  }
  return KeystoreConfig(
    keyAlias = getRequiredProperty("${prefix}_KEY_ALIAS"),
    keyPassword = getRequiredProperty("${prefix}_KEY_PASSWORD"),
    storeFile = getRequiredProperty("${prefix}_STORE_FILE"),
    storePassword = getRequiredProperty("${prefix}_STORE_PASSWORD")
  )
}
