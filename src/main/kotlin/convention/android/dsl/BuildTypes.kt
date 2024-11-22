package convention.android.dsl

import com.android.build.api.dsl.BuildType
import org.gradle.api.NamedDomainObjectContainer

public const val BUILD_TYPE_DEBUG: String = "debug"
public const val BUILD_TYPE_QA: String = "qa"
public const val BUILD_TYPE_STAGING: String = "staging"
public const val BUILD_TYPE_RELEASE: String = "release"

public enum class BuildTypeSuffix(
  public val suffix: String? = null,
) {
  DEBUG(".$BUILD_TYPE_DEBUG"),
  QA(".$BUILD_TYPE_DEBUG"),
  STAGING,
  RELEASE,
}

/**
 * Extension function for the NamedDomainObjectContainer class.
 * This function is used to configure the qa build type.
 *
 * @param action A lambda with receiver of type BuildTypeT. This lambda is used to configure the qa build type.
 * @return The configured qa build type.
 */
public fun <BuildTypeT : BuildType> NamedDomainObjectContainer<BuildTypeT>.qa(
  action: BuildTypeT.() -> Unit,
): BuildTypeT = getByName(BUILD_TYPE_QA, action)

/**
 * Extension function for the NamedDomainObjectContainer class.
 * This function is used to configure the staging build type.
 *
 * @param action A lambda with receiver of type BuildTypeT. This lambda is used to configure the staging build type.
 * @return The configured staging build type.
 */
public fun <BuildTypeT : BuildType> NamedDomainObjectContainer<BuildTypeT>.staging(
  action: BuildTypeT.() -> Unit,
): BuildTypeT = getByName(BUILD_TYPE_STAGING, action)
