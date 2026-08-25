package convention.icons.svg2compose.core.vector

import com.squareup.kotlinpoet.MemberName
import convention.icons.svg2compose.core.MemberNames

public enum class FillType(public val memberName: MemberName) {
  NonZero(MemberNames.NonZero),
  EvenOdd(MemberNames.EvenOdd)
}
