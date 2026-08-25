package convention.icons.svg2compose.core

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import java.io.File

public typealias IconGroup = String

public class IconWriter(
  private val icons: Collection<Icon>,
  private val groupClass: ClassName,
  private val groupPackage: String,
  private val generatePreview: Boolean
) {

  public fun generateTo(
    outputSrcDirectory: File,
    iconNamePredicate: (String) -> Boolean
  ): List<MemberName> {

    return icons.filter { icon ->
      val iconName = icon.kotlinName

      iconNamePredicate(iconName)
    }.map { icon ->
      val iconName = icon.kotlinName

      val vector = IconParser(icon).parse()

      val (fileSpec, accessProperty) = VectorAssetGenerator(
        iconName,
        groupPackage,
        vector,
        generatePreview
      ).createFileSpec(groupClass)

      fileSpec.writeTo(outputSrcDirectory)

      MemberName(fileSpec.packageName, accessProperty)
    }
  }
}
