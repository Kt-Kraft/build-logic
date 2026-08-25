package convention.icons.svg2compose.core

import com.squareup.kotlinpoet.ClassName

public enum class IconTheme(public val themePackageName: String, public val themeClassName: String) {
  Filled("filled", "Filled"),
  Outlined("outlined", "Outlined"),
  Rounded("rounded", "Rounded"),
  TwoTone("twotone", "TwoTone"),
  Sharp("sharp", "Sharp")
}

public fun String.toIconTheme(): IconTheme = requireNotNull(IconTheme.entries.find {
  it.themePackageName == this
}) { "No matching theme found" }

public val IconTheme.className: ClassName get() = PackageNames.MaterialIconsPackage.className("Icons", themeClassName)
