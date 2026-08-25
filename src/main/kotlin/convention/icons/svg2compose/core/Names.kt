package convention.icons.svg2compose.core

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

public enum class PackageNames(public val packageName: String) {
  MaterialIconsPackage("androidx.compose.material.icons"),
  UiPackage("androidx.compose.ui"),
  GraphicsPackage(UiPackage.packageName + ".graphics"),
  VectorPackage(GraphicsPackage.packageName + ".vector"),
  GeometryPackage(UiPackage.packageName + ".geometry"),
  Unit(UiPackage.packageName + ".unit"),
  FoundationPackage("androidx.compose.foundation"),
  LayoutPackage(FoundationPackage.packageName + ".layout"),
  PreviewPackage(UiPackage.packageName + ".tooling.preview"),
  RuntimePackage("androidx.compose.runtime"),
}

public object ClassNames {
  public val Icons: ClassName = PackageNames.MaterialIconsPackage.className("Icons")
  public val ImageVector: ClassName = PackageNames.VectorPackage.className("ImageVector")
  public val PathFillType: ClassName = PackageNames.GraphicsPackage.className("PathFillType", CompanionImportName)
  public val StrokeCap: ClassName = PackageNames.GraphicsPackage.className("StrokeCap", CompanionImportName)
  public val StrokeJoin: ClassName = PackageNames.GraphicsPackage.className("StrokeJoin", CompanionImportName)
  public val Brush: ClassName = PackageNames.GraphicsPackage.className("Brush", CompanionImportName)
  public val Preview: ClassName = PackageNames.PreviewPackage.className("Preview")
  public val Composable: ClassName = PackageNames.RuntimePackage.className("Composable")
}

public object MemberNames {
  public val ImageVectorBuilder: MemberName = MemberName(ClassNames.ImageVector, "Builder")

  public val Path: MemberName = MemberName(PackageNames.VectorPackage.packageName, "path")

  public val EvenOdd: MemberName = MemberName(ClassNames.PathFillType, "EvenOdd")
  public val NonZero: MemberName = MemberName(ClassNames.PathFillType, "NonZero")

  public val Group: MemberName = MemberName(PackageNames.VectorPackage.packageName, "group")

  public val StrokeCapButt: MemberName = MemberName(ClassNames.StrokeCap, "Butt")
  public val StrokeCapRound: MemberName = MemberName(ClassNames.StrokeCap, "Round")
  public val StrokeCapSquare: MemberName = MemberName(ClassNames.StrokeCap, "Square")

  public val StrokeJoinMiter: MemberName = MemberName(ClassNames.StrokeJoin, "Miter")
  public val StrokeJoinRound: MemberName = MemberName(ClassNames.StrokeJoin, "Round")
  public val StrokeJoinBevel: MemberName = MemberName(ClassNames.StrokeJoin, "Bevel")

  public val Dp: MemberName = MemberName(PackageNames.Unit.packageName, "dp")

  public val Color: MemberName = MemberName(PackageNames.GraphicsPackage.packageName, "Color")
  public val SolidColor: MemberName = MemberName(PackageNames.GraphicsPackage.packageName, "SolidColor")

  public val LinearGradient: MemberName = MemberName(ClassNames.Brush, "linearGradient")
  public val RadialGradient: MemberName = MemberName(ClassNames.Brush, "radialGradient")

  public val Offset: MemberName = MemberName(PackageNames.GeometryPackage.packageName, "Offset")
}

public fun PackageNames.className(vararg classNames: String): ClassName = ClassName(this.packageName, *classNames)

private const val CompanionImportName = "Companion"
