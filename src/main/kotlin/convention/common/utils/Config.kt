package convention.common.utils

public object Config {
  public val optIns: List<String> = listOf(
    "kotlin.RequiresOptIn",
    "kotlin.experimental.ExperimentalTypeInference",
    "kotlin.uuid.ExperimentalUuidApi",
    "kotlin.contracts.ExperimentalContracts",

    // TODO: This only applied on compose project with resource api
    //"kotlinx.coroutines.ExperimentalCoroutinesApi",
    // TODO: Check this in future development
    //"kotlinx.coroutines.FlowPreview",
    //"org.jetbrains.compose.resources.ExperimentalResourceApi"
  )

  public val compilerArgs: List<String> = listOf(
    "-Xexpect-actual-classes",
    "-Xconsistent-data-class-copy-visibility",
    "-Xsuppress-warning=NOTHING_TO_INLINE",
    "-Xsuppress-warning=UNUSED_ANONYMOUS_PARAMETER",

    // TODO: Check this in future development
    //"-Xbackend-threads=0", // parallel IR compilation
    //"-Xwasm-use-new-exception-proposal",
    //"-Xwasm-debugger-custom-formatters"
  )

  public val jvmCompilerArgs: List<String> = buildList {
    addAll(compilerArgs)
    add("-Xjvm-default=all") // enable all jvm optimizations
    add("-Xcontext-receivers")
    add("-Xstring-concat=inline")
    add("-Xlambdas=indy")

    // TODO: Check this in future development
    //add("-Xjdk-release=${jvmTarget}")
  }
}
