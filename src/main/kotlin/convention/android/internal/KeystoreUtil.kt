package convention.android.internal

import convention.android.model.KeystoreConfig
import java.util.Properties

internal fun Properties.loadKeystoreConfig(prefix: String): KeystoreConfig {
  val keyAlias = getProperty("${prefix}_KEY_ALIAS")
    ?: error("${prefix}_KEY_ALIAS is required in keystore.properties")
  val keyPassword = getProperty("${prefix}_KEY_PASSWORD")
    ?: error("${prefix}_KEY_PASSWORD is required in keystore.properties")
  val storeFile = getProperty("${prefix}_STORE_FILE")
    ?: error("${prefix}_STORE_FILE is required in keystore.properties")
  val storePassword = getProperty("${prefix}_STORE_PASSWORD")
    ?: error("${prefix}_STORE_PASSWORD is required in keystore.properties")
  return KeystoreConfig(keyAlias, keyPassword, storeFile, storePassword)
}
