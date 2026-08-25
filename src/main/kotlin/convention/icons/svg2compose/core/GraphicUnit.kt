package convention.icons.svg2compose.core

import com.squareup.kotlinpoet.MemberName

public sealed class GraphicUnit {
  public abstract val value: Float
  public abstract val memberName: MemberName?
}

public class Pixel(override val value: Float) : GraphicUnit() {
  override val memberName: MemberName? = null
}

public class Dp(override val value: Float) : GraphicUnit() {
  override val memberName: MemberName? = MemberNames.Dp
}

public fun rawAsGraphicUnit(raw: String): GraphicUnit {
  val isStrokeDp = raw.endsWith("dp")
  return when {
    isStrokeDp -> Dp(raw.removeSuffix("dp").toFloat())
    else -> Pixel(raw.toFloat())
  }
}
