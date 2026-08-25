package convention.icons.svg2compose.core.vector

import com.squareup.kotlinpoet.MemberName
import convention.icons.svg2compose.core.MemberNames

public enum class StrokeJoin(public val memberName: MemberName) {
  Miter(MemberNames.StrokeJoinMiter),
  Round(MemberNames.StrokeJoinRound),
  Bevel(MemberNames.StrokeJoinBevel)
}
