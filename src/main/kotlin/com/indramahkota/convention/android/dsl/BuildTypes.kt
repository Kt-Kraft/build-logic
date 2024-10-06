package com.indramahkota.convention.android.dsl

import com.android.build.api.dsl.BuildType
import org.gradle.api.NamedDomainObjectContainer

/**
 * Represents the debug build type. This is typically used during development.
 */
public const val BUILD_TYPE_DEBUG: String = "debug"

/**
 * Represents the debug build type. This is typically used during development.
 */
public const val BUILD_TYPE_QA: String = "qa"

/**
 * Represents a special release-staging build type. This can be used for staging or testing the release version.
 */
public const val BUILD_TYPE_STAGING: String = "staging"

/**
 * Represents the release build type. This is typically used for the production version of the application.
 */
public const val BUILD_TYPE_RELEASE: String = "release"

/**
 * Enum class representing the different build type suffixes.
 * Each enum entry corresponds to a different build type and contains an optional suffix string.
 *
 * @property suffix The optional suffix string for the build type. This is typically used to append to the build type name.
 */
public enum class BuildTypeSuffix(
  public val suffix: String? = null,
) {
  /**
   * Represents the debug build type suffix.
   * The suffix is the debug build type name prefixed with a dot.
   */
  DEBUG(".$BUILD_TYPE_DEBUG"),

  /**
   * Represents the debug build type suffix.
   * The suffix is the debug build type name prefixed with a dot.
   */
  QA(".$BUILD_TYPE_DEBUG"),

  /**
   * Represents the staging build type suffix.
   * This does not have a suffix string.
   */
  STAGING,

  /**
   * Represents the release build type suffix.
   * This does not have a suffix string.
   */
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
