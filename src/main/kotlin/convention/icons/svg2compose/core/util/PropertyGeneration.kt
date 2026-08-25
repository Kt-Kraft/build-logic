package convention.icons.svg2compose.core.util

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.buildCodeBlock

internal fun backingPropertySpec(name: String, type: TypeName): PropertySpec {
  val nullableVectorAsset = type.copy(nullable = true)
  return PropertySpec.builder(name = name, type = nullableVectorAsset)
    .mutable()
    .addModifiers(KModifier.PRIVATE)
    .initializer("null")
    .build()
}

internal inline fun FunSpec.Builder.withBackingProperty(
  backingProperty: PropertySpec,
  block: FunSpec.Builder.() -> Unit
): FunSpec.Builder = apply {
  addCode(buildCodeBlock {
    beginControlFlow("if (%N != null)", backingProperty)
    addStatement("return %N!!", backingProperty)
    endControlFlow()
  })
    .apply(block)
    .addStatement("return %N!!", backingProperty)
}
