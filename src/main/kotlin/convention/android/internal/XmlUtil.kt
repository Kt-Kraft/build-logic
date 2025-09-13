package convention.android.internal

import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import net.pearx.kasechange.toSnakeCase
import org.w3c.dom.Document

internal fun Document.getAppName(): String? {
  val stringElements = documentElement.getElementsByTagName("string")
  return (0 until stringElements.length).firstNotNullOfOrNull { index ->
    val node = stringElements.item(index)
    val nameAttribute = node?.attributes?.getNamedItem("name")?.nodeValue
    node?.textContent.takeIf { nameAttribute == "app_name" && !it.isNullOrBlank() }
  }
}

internal fun Document.createAppNameSuffix(variant: String, extension: String): String {
  val versionCode = documentElement.getAttribute("android:versionCode")
  val versionName = documentElement.getAttribute("android:versionName")
  val formattedDateTime = LocalDateTime.now(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("ddMMMyyyy'T'HH.mm", Locale.getDefault()))
  return "V$versionName-$versionCode-$formattedDateTime-${variant.toSnakeCase().uppercase()}.$extension"
}

internal fun File.createNewAppName(
  stringsDirectory: File,
  variantName: String,
  extension: String,
): String {
  val manifest = this.readText()
  val manifestDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    .parse(manifest.byteInputStream())
  val stringsFile = stringsDirectory.readText()
  val stringsDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    .parse(stringsFile.byteInputStream())
  val appName = stringsDocument.getAppName().orEmpty().replace(" ", "-")
  val suffix = manifestDocument.createAppNameSuffix(variant = variantName, extension = extension)
  return "$appName-$suffix"
}
