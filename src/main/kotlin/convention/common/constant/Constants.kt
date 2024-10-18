package convention.common.constant

import convention.common.annotation.InternalPluginApi

/**
 * Constant for the Java plugin ID.
 */
@InternalPluginApi
public const val PLUGIN_ID_JAVA: String = "java"

/**
 * Constant for the Kotlin Android plugin ID.
 */
@InternalPluginApi
public const val PLUGIN_ID_KOTLIN_ANDROID: String = "org.jetbrains.kotlin.android"

/**
 * Constant for the Android Application plugin ID.
 */
@InternalPluginApi
public const val PLUGIN_ID_ANDROID_APPLICATION: String = "com.android.application"

/**
 * Constant for the Android Library plugin ID.
 */
@InternalPluginApi
public const val PLUGIN_ID_ANDROID_LIBRARY: String = "com.android.library"

/**
 * Constant for the Gradle Version Catalog plugin ID.
 */
@InternalPluginApi
public const val PLUGIN_ID_VERSION_CATALOG: String = "org.gradle.version-catalog"

/**
 * Constant for the Kotlin Compose Compiler plugin ID.
 */
@InternalPluginApi
public const val PLUGIN_ID_KOTLIN_COMPOSE_COMPILER: String = "org.jetbrains.kotlin.plugin.compose"

/**
 * Constant for the Java Gradle Plugin ID.
 */
@InternalPluginApi
public const val PLUGIN_ID_JAVA_GRADLE_PLUGIN: String = "java-gradle-plugin"

/**
 * Constant for the Maven Publish plugin ID.
 */
@InternalPluginApi
public const val PLUGIN_ID_MAVEN_PUBLISH: String = "maven-publish"

/**
 * Constant for the Convention Android App plugin ID.
 */
@InternalPluginApi
public const val PLUGIN_ID_CONVENTION_ANDROID_APP: String = "android.app"

/**
 * Constant for the Convention Android Library plugin ID.
 */
@InternalPluginApi
public const val PLUGIN_ID_CONVENTION_ANDROID_LIB: String = "android.lib"

/**
 * Constant for the Google Maps Platform Secrets Gradle plugin ID.
 * This plugin is used to manage secrets for Google Maps Platform in a secure way.
 */
@InternalPluginApi
public const val SECRET_GRADLEW_PLUGIN: String = "com.google.android.libraries.mapsplatform.secrets-gradle-plugin"
