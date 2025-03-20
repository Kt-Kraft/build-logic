package convention.android.dsl

import com.android.build.api.dsl.BuildType
import org.gradle.api.NamedDomainObjectContainer

public const val BUILD_TYPE_DEBUG: String = "debug"
public const val BUILD_TYPE_RELEASE: String = "release"
public const val BUILD_TYPE_PROFILE: String = "profile"

public enum class BuildTypeSuffix(
  public val suffix: String? = null,
) {
  DEBUG(".$BUILD_TYPE_DEBUG"),
  RELEASE,
  PROFILE,
}

/**
 * Extension function for the NamedDomainObjectContainer class.
 * This function is used to configure the profile build type.
 *
 * @param action A lambda with receiver of type BuildTypeT. This lambda is used to configure the staging profile type.
 * @return The configured profile build type.
 */
public fun <BuildTypeT : BuildType> NamedDomainObjectContainer<BuildTypeT>.profile(
  action: BuildTypeT.() -> Unit,
): BuildTypeT = getByName(BUILD_TYPE_PROFILE, action)
