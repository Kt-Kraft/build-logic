package convention.common.utils

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
    "-Xsuppress-warning=NOTHING_TO_INLINE",
    "-Xsuppress-warning=UNUSED_ANONYMOUS_PARAMETER",
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
