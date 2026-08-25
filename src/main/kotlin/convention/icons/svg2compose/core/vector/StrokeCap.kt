package convention.icons.svg2compose.core.vector

import com.squareup.kotlinpoet.MemberName
import convention.icons.svg2compose.core.MemberNames

public enum class StrokeCap(public val memberName: MemberName) {
  Butt(MemberNames.StrokeCapButt),
  Round(MemberNames.StrokeCapRound),
  Square(MemberNames.StrokeCapSquare)
}
