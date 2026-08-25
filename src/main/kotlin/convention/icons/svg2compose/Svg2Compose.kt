package convention.icons.svg2compose

import com.android.ide.common.vectordrawable.Svg2Vector
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.MemberName
import convention.icons.svg2compose.core.AllIconAccessorGenerator
import convention.icons.svg2compose.core.Icon
import convention.icons.svg2compose.core.IconGroupGenerator
import convention.icons.svg2compose.core.IconWriter
import convention.icons.svg2compose.core.toKotlinPropertyName
import java.io.File
import java.util.Locale
import java.util.Stack
import kotlin.io.path.createTempDirectory

public typealias IconNameTransformer = (iconName: String, group: String) -> String

public object Svg2Compose {

  public fun parse(
    applicationIconPackage: String,
    accessorName: String,
    outputSourceDirectory: File,
    vectorsDirectory: File,
    type: VectorType = VectorType.SVG,
    iconNameTransformer: IconNameTransformer = { it, _ -> it.toKotlinPropertyName() },
    allAssetsPropertyName: String = "AllAssets",
    generatePreview: Boolean = true,
    generateStringAccessor: Boolean = false,
  ): ParsingResult {
    fun nameRelative(vectorFile: File) = vectorFile.relativeTo(vectorsDirectory).path

    val drawableDir = drawableTempDirectory()

    val groupStack = Stack<GeneratedGroup>()

    vectorsDirectory.walkTopDown()
      .maxDepth(10)
      .onEnter { file ->
        val dirIcons = file.listFiles()
          .filter { it.isDirectory.not() }
          .filter { it.extension.equals(type.extension, ignoreCase = true) }

        val previousGroup = groupStack.peekOrNull()

        // if there is no previous group, this is the root dir, and the group name should be the accessorName
        val groupName = if (previousGroup == null) accessorName else file.name.toKotlinPropertyName()
        val groupPackage =
          previousGroup?.let { group -> "${group.groupPackage}.${group.groupName.second.lowercase(Locale.getDefault())}" }
            ?: applicationIconPackage
        val iconsPackage = "$groupPackage.${groupName.lowercase(Locale.getDefault())}"

        val (groupFileSpec, groupClassName) = IconGroupGenerator(
          groupPackage,
          groupName,
          generateStringAccessor,
        ).createFileSpec(previousGroup?.groupClass)

        val generatedIconsMemberNames: Map<VectorFile, MemberName> =
          if (dirIcons.isNotEmpty()) {
            val drawables: List<Pair<File, File>> = when (type) {
              VectorType.SVG -> dirIcons.map {
                val iconName = nameRelative(it).withoutExtension

                val parsedFile = File(drawableDir, "${iconName}.xml")
                parsedFile.parentFile.mkdirs()

                Svg2Vector.parseSvgToXml(it.toPath(), parsedFile.outputStream())

                it to parsedFile
              }.toList()

              VectorType.DRAWABLE -> dirIcons.toList().map { it to it }
            }

            val icons: Map<VectorFile, Icon> = drawables.associate { (vectorFile, drawableFile) ->
              vectorFile to Icon(
                iconNameTransformer(
                  drawableFile.nameWithoutExtension.trim(),
                  groupName
                ),
                drawableFile.name,
                drawableFile.readText()
              )
            }

            val writer = IconWriter(
              icons.values,
              groupClassName,
              iconsPackage,
              generatePreview
            )

            val memberNames = writer.generateTo(outputSourceDirectory) { true }

            icons.mapValues { entry ->
              memberNames.first { it.simpleName == entry.value.kotlinName }
            }
          } else {
            emptyMap()
          }

        val result = GeneratedGroup(
          groupPackage,
          file to groupName,
          generatedIconsMemberNames,
          groupClassName,
          groupFileSpec,
          childGroups = emptyList()
        )

        if (previousGroup != null) {
          groupStack.pop()
          groupStack.push(previousGroup.copy(childGroups = previousGroup.childGroups + result))
        }

        groupStack.push(result)

        true
      }
      .onLeave {
        val group = if (groupStack.size > 1)
          groupStack.pop()
        else
          groupStack.peek()

        val allAssetsGenerator = AllIconAccessorGenerator(
          group.generatedIconsMemberNames.values.sortedBy { it.simpleName },
          group.groupClass,
          allAssetsPropertyName,
          group.childGroups,
          generateStringAccessor,
        )

        for (propertySpec in allAssetsGenerator.createPropertySpec(group.groupFileSpec)) {
          group.groupFileSpec.addProperty(propertySpec)
        }

        group.groupFileSpec.build().writeTo(outputSourceDirectory)
      }
      .toList() // consume, to onEnter and onLeave be triggered

    return groupStack.pop().asParsingResult()
  }

  private fun drawableTempDirectory() =
    createTempDirectory(prefix = "svg2compose-").toFile()

  private val String.withoutExtension get() = substringBeforeLast(".")
}

public typealias GroupFolder = File
public typealias VectorFile = File

public data class GeneratedGroup(
  val groupPackage: String,
  val groupName: Pair<GroupFolder, String>,
  val generatedIconsMemberNames: Map<VectorFile, MemberName>,
  val groupClass: ClassName,
  val groupFileSpec: FileSpec.Builder,
  val childGroups: List<GeneratedGroup>
)

public data class ParsingResult(
  val groupName: Pair<GroupFolder, String>,
  val generatedIconsMemberNames: Map<VectorFile, MemberName>,
  val generatedGroups: List<ParsingResult>
)

private fun GeneratedGroup.asParsingResult(): ParsingResult = ParsingResult(
  groupName = groupName,
  generatedIconsMemberNames = generatedIconsMemberNames,
  generatedGroups = childGroups.map { it.asParsingResult() },
)
