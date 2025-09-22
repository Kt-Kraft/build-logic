package convention.swiftinterop.tasks

internal sealed interface XcodebuildBuildTarget {
  val destination: String

  enum class Generic(
    private val platform: String,
    val disambiguationClassifier: String,
  ) : XcodebuildBuildTarget {
    MACOS("macOS", "macos"),
    IOS("iOS", "ios"),
    IOS_SIMULATOR("iOS Simulator", "iosSimulator"),
    TVOS("tvOS", "tvos"),
    TVOS_SIMULATOR("tvOS Simulator", "tvosSimulator"),
    WATCHOS("watchOS", "watchos"),
    WATCHOS_SIMULATOR("watchOS Simulator", "watchosSimulator");

    override val destination: String get() = "generic/platform=$platform"
  }
}
