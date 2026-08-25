package convention.icons.svg2compose.core

import com.squareup.kotlinpoet.FileSpec
import java.io.File
import java.nio.file.Files

public fun FileSpec.writeToWithCopyright(directory: File, textTransform: ((String) -> String)? = null) {
  var outputDirectory = directory

  if (packageName.isNotEmpty()) {
    for (packageComponent in packageName.split('.').dropLastWhile { it.isEmpty() }) {
      outputDirectory = outputDirectory.resolve(packageComponent)
    }
  }

  Files.createDirectories(outputDirectory.toPath())

  val file = outputDirectory.resolve("$name.kt")

  // Write this FileSpec to a StringBuilder, so we can process the text before writing to file.
  val fileContent = StringBuilder().run {
    writeTo(this)
    toString()
  }

  val transformedText = textTransform?.invoke(fileContent) ?: fileContent

  file.writeText(transformedText)
}

public fun FileSpec.Builder.setIndent(): FileSpec.Builder = indent(Indent)

// Code style indent is 4 spaces, compared to KotlinPoet's default of 2
private val Indent = " ".repeat(2)
