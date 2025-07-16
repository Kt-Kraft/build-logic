package convention.common.utils

import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerToolOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

public object Config {
  public val optIns: List<String> = listOf(
    "kotlin.RequiresOptIn",
    "kotlin.experimental.ExperimentalTypeInference",
    "kotlin.uuid.ExperimentalUuidApi",
    "kotlin.contracts.ExperimentalContracts",
  )

  public val compilerArgs: List<String> = listOf(
    "-Xexpect-actual-classes",
    "-Xconsistent-data-class-copy-visibility",
    "-Xwarning-level=NOTHING_TO_INLINE:disabled",
    "-Xwarning-level=UNUSED_ANONYMOUS_PARAMETER:disabled",
    "-Xcontext-parameters",
  )

  public val jvmCompilerArgs: List<String> = buildList {
    addAll(compilerArgs)
    add("-Xjvm-default=all") // enable all jvm optimizations
    add("-Xcontext-parameters")
    add("-Xstring-concat=inline")
    add("-Xlambdas=indy")
  }
}

internal fun List<String>.mergedDistinctWith(other: List<String>): List<String> {
  return (this + other).distinct()
}

internal fun KotlinJvmCompilerOptions.addDistinctCompilerArgs(newArgs: List<String>) {
  val merged = freeCompilerArgs.getOrElse(emptyList()).mergedDistinctWith(newArgs)
  freeCompilerArgs.set(merged)
}

internal fun KotlinCommonCompilerToolOptions.addDistinctCompilerArgs(newArgs: List<String>) {
  val merged = freeCompilerArgs.getOrElse(emptyList()).mergedDistinctWith(newArgs)
  freeCompilerArgs.set(merged)
}

internal fun KotlinJvmCompilerOptions.addDistinctOptIns(newOptIns: List<String>) {
  val merged = optIn.getOrElse(emptyList()).mergedDistinctWith(newOptIns)
  optIn.set(merged)
}

internal fun KotlinCommonCompilerOptions.addDistinctOptIns(newOptIns: List<String>) {
  val merged = optIn.getOrElse(emptyList()).mergedDistinctWith(newOptIns)
  optIn.set(merged)
}
