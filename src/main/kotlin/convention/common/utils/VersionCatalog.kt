package convention.common.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency

public val Project.versionCatalog: Lazy<VersionCatalog>
  get() = lazy { extensions.getByType<VersionCatalogsExtension>().named("libs") }

public fun VersionCatalog.requirePlugin(alias: String): String = findPlugin(alias).get().toString()
public fun VersionCatalog.requireLib(alias: String): Provider<MinimalExternalModuleDependency> = findLibrary(alias).get()
public fun VersionCatalog.requireBundle(alias: String): Provider<ExternalModuleDependencyBundle> = findBundle(alias).get()
public fun VersionCatalog.requireVersion(alias: String): String = findVersion(alias).get().toString()
public val Provider<PluginDependency>.id: String get() = get().pluginId
