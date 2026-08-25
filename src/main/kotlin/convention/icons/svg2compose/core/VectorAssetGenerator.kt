package convention.icons.svg2compose.core

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import convention.icons.svg2compose.core.util.backingPropertySpec
import convention.icons.svg2compose.core.util.withBackingProperty
import convention.icons.svg2compose.core.vector.Fill
import convention.icons.svg2compose.core.vector.Vector
import convention.icons.svg2compose.core.vector.VectorNode
import java.util.Locale

public data class VectorAssetGenerationResult(
  val sourceGeneration: FileSpec, val accessProperty: String
)

public class VectorAssetGenerator(
  private val iconName: String,
  private val iconGroupPackage: String,
  private val vector: Vector,
  private val generatePreview: Boolean
) {

  public fun createFileSpec(groupClassName: ClassName): VectorAssetGenerationResult {
    // Use a unique property name for the private backing property. This is because (as of
    // Kotlin 1.4) each property with the same name will be considered as a possible candidate
    // for resolution, regardless of the access modifier, so by using unique names we reduce
    // the size from ~6000 to 1, and speed up compilation time for these icons.
    val backingPropertyName = "_" + iconName.replaceFirstChar { it.lowercase(Locale.ROOT) }
    val backingProperty = backingPropertySpec(name = backingPropertyName, ClassNames.ImageVector)

    val generation = FileSpec.builder(
      packageName = iconGroupPackage,
      fileName = iconName
    ).addProperty(
      PropertySpec.builder(name = iconName, type = ClassNames.ImageVector)
        .receiver(groupClassName)
        .getter(iconGetter(backingProperty))
        .build()
    ).addProperty(
      backingProperty
    )
      .apply { if (generatePreview) addFunction(iconPreview(groupClassName.simpleName, iconName)) }
      .setIndent().build()

    return VectorAssetGenerationResult(generation, iconName)
  }

  private fun iconGetter(backingProperty: PropertySpec): FunSpec {

    val parameterList = with(vector) {
      listOfNotNull(
        "name = \"${iconName}\"",
        "defaultWidth = ${width.withMemberIfNotNull}",
        "defaultHeight = ${height.withMemberIfNotNull}",
        "viewportWidth = ${viewportWidth}f",
        "viewportHeight = ${viewportHeight}f"
      )
    }

    val parameters = parameterList.joinToString(prefix = "(", postfix = ")")

    val members: Array<Any> = listOfNotNull(
      MemberNames.ImageVectorBuilder,
      vector.width.memberName,
      vector.height.memberName
    ).toTypedArray()

    return FunSpec.getterBuilder()
      .withBackingProperty(backingProperty) {
        addCode(buildCodeBlock {
          beginControlFlow(
            "%N = %M$parameters.apply",
            backingProperty,
            *members
          )
          vector.nodes.forEach { node -> addRecursively(node) }
          endControlFlow()
          addStatement(".build()")
        })
      }
      .build()
  }

  private fun iconPreview(groupName: String, iconName: String): FunSpec {
    val previewAnnotation = AnnotationSpec.builder(ClassNames.Preview).build()
    val composableAnnotation = AnnotationSpec.builder(ClassNames.Composable).build()
    val box = MemberName(PackageNames.LayoutPackage.packageName, "Box")
    val modifier = MemberName(PackageNames.UiPackage.packageName, "Modifier")
    val padding = MemberName(PackageNames.LayoutPackage.packageName, "padding")
    val paddingValue = MemberNames.Dp
    val composeImage = MemberName(PackageNames.FoundationPackage.packageName, "Image")

    return FunSpec.builder("Preview")
      .addModifiers(KModifier.PRIVATE)
      .addAnnotation(previewAnnotation)
      .addAnnotation(composableAnnotation)
      .addCode(buildCodeBlock {
        beginControlFlow("%M(modifier = %M.%M(12.%M))", box, modifier, padding, paddingValue)
        addStatement("%M(imageVector = $groupName.$iconName, contentDescription = \"\")", composeImage)
        endControlFlow()
      })
      .build()
  }
}

private fun CodeBlock.Builder.addRecursively(vectorNode: VectorNode) {
  when (vectorNode) {
    // TODO: b/147418351 - add clip-paths once they are supported
    is VectorNode.Group -> {
      beginControlFlow("%M", MemberNames.Group)
      vectorNode.paths.forEach { path ->
        addRecursively(path)
      }
      endControlFlow()
    }

    is VectorNode.Path -> {
      addPath(vectorNode) {
        vectorNode.nodes.forEach { pathNode ->
          addStatement(pathNode.asFunctionCall())
        }
      }
    }
  }
}

private fun CodeBlock.Builder.addPath(
  path: VectorNode.Path,
  pathBody: CodeBlock.Builder.() -> Unit
) {
  val hasStrokeColor = path.strokeColorHex != null

  val parameterList = with(path) {
    listOfNotNull(
      "fill = ${getPathFill(path)}",
      "stroke = ${if (hasStrokeColor) "%M(%M(0x$strokeColorHex))" else "null"}",
      "fillAlpha = ${fillAlpha}f".takeIf { fillAlpha != 1f },
      "strokeAlpha = ${strokeAlpha}f".takeIf { strokeAlpha != 1f },
      "strokeLineWidth = ${strokeLineWidth.withMemberIfNotNull}",
      "strokeLineCap = %M",
      "strokeLineJoin = %M",
      "strokeLineMiter = ${strokeLineMiter}f",
      "pathFillType = %M"
    )
  }

  val parameters = parameterList.joinToString(prefix = "(", postfix = ")")

  val members: Array<Any> = listOfNotNull(
    MemberNames.Path,
    MemberNames.SolidColor.takeIf { hasStrokeColor },
    MemberNames.Color.takeIf { hasStrokeColor },
    path.strokeLineWidth.memberName,
    path.strokeLineCap.memberName,
    path.strokeLineJoin.memberName,
    path.fillType.memberName
  ).toMutableList().apply {
    var fillIndex = 1
    when (path.fill) {
      is Fill.Color -> {
        add(fillIndex, MemberNames.SolidColor)
        add(++fillIndex, MemberNames.Color)
      }

      is Fill.LinearGradient -> {
        add(fillIndex, MemberNames.LinearGradient)
        path.fill.colorStops.forEach { _ ->
          add(++fillIndex, MemberNames.Color)
        }
        add(++fillIndex, MemberNames.Offset)
        add(++fillIndex, MemberNames.Offset)
      }

      is Fill.RadialGradient -> {
        add(fillIndex, MemberNames.RadialGradient)
        path.fill.colorStops.forEach { _ ->
          add(++fillIndex, MemberNames.Color)
        }
        add(++fillIndex, MemberNames.Offset)
      }

      null -> {}
    }
  }.toTypedArray()

  beginControlFlow(
    "%M$parameters",
    *members
  )

  pathBody()
  endControlFlow()
}

private fun getPathFill(
  path: VectorNode.Path
) = when (path.fill) {
  is Fill.Color -> "%M(%M(0x${path.fill.colorHex}))"
  is Fill.LinearGradient -> {
    with(path.fill) {
      "%M(" +
        "${getGradientStops(path.fill.colorStops).toString().removeSurrounding("[", "]")}, " +
        "start = %M(${startX}f,${startY}f), " +
        "end = %M(${endX}f,${endY}f))"
    }
  }

  is Fill.RadialGradient -> {
    with(path.fill) {
      "%M(${getGradientStops(path.fill.colorStops).toString().removeSurrounding("[", "]")}, " +
        "center = %M(${centerX}f,${centerY}f), " +
        "radius = ${gradientRadius}f)"
    }
  }

  else -> "null"
}

private fun getGradientStops(
  stops: List<Pair<Float, String>>
) = stops.map { stop ->
  "${stop.first}f to %M(0x${stop.second})"
}

private val GraphicUnit.withMemberIfNotNull: String get() = "${value}${if (memberName != null) ".%M" else "f"}"
