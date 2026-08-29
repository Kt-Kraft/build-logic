package convention.common.utils

import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerToolOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

internal fun List<String>.mergedDistinctWith(other: List<String>): List<String> {
  return (this + other).distinct()
}

internal fun KotlinJvmCompilerOptions.addDistinctCompilerArgs(newArgs: List<String>) {
  val merged = freeCompilerArgs.getOrElse(mutableListOf()).mergedDistinctWith(newArgs)
  freeCompilerArgs.set(merged)
}

internal fun KotlinCommonCompilerToolOptions.addDistinctCompilerArgs(newArgs: List<String>) {
  val merged = freeCompilerArgs.getOrElse(mutableListOf()).mergedDistinctWith(newArgs)
  freeCompilerArgs.set(merged)
}

internal fun KotlinJvmCompilerOptions.addDistinctOptIns(newOptIns: List<String>) {
  val merged = optIn.getOrElse(mutableListOf()).mergedDistinctWith(newOptIns)
  optIn.set(merged)
}

internal fun KotlinCommonCompilerOptions.addDistinctOptIns(newOptIns: List<String>) {
  val merged = optIn.getOrElse(mutableListOf()).mergedDistinctWith(newOptIns)
  optIn.set(merged)
}
