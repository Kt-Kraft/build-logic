package convention.icons.svg2compose.core.vector

import convention.icons.svg2compose.core.GraphicUnit

public class Vector(
  public val width: GraphicUnit,
  public val height: GraphicUnit,
  public val viewportWidth: Float,
  public val viewportHeight: Float,
  public val nodes: List<VectorNode>
)

public sealed class VectorNode {
  public class Group(public val paths: MutableList<Path> = mutableListOf()) : VectorNode()
  public data class Path(
    val fill: Fill?,
    val strokeColorHex: String?,
    val strokeAlpha: Float,
    val fillAlpha: Float,
    val strokeLineWidth: GraphicUnit,
    val strokeLineCap: StrokeCap,
    val strokeLineJoin: StrokeJoin,
    val strokeLineMiter: Float,
    val fillType: FillType,
    val nodes: List<PathNode>
  ) : VectorNode()
}

public sealed class Fill {
  public data class Color(val colorHex: String) : Fill()
  public data class LinearGradient(
    val startY: Float,
    val startX: Float,
    val endY: Float,
    val endX: Float,
    val colorStops: MutableList<Pair<Float, String>> = mutableListOf()
  ) : Fill()

  public data class RadialGradient(
    val gradientRadius: Float,
    val centerX: Float,
    val centerY: Float,
    val colorStops: MutableList<Pair<Float, String>> = mutableListOf()
  ) : Fill()
}
