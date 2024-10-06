package com.indramahkota.convention.common

import com.indramahkota.convention.common.internal.findExtByName
import javax.inject.Inject
import org.gradle.api.JavaVersion
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Property

/**
 * This is an abstract class that represents a convention extension.
 * It implements the ExtensionAware and WithDefaults interfaces with the ConventionExtension type parameter.
 *
 * @property jvmTarget The target Java version for the project. It is a Property of JavaVersion type.
 * @property configsDir The directory for the project's configuration files. It is a DirectoryProperty.
 * @property reportsDir The directory for the project's report files. It is a DirectoryProperty.
 *
 * @constructor Creates a new instance of the ConventionExtension class.
 * @param layout The project layout.
 * @param objects The object factory.
 *
 * @throws Inject If the dependencies cannot be injected.
 */
public abstract class ConventionExtension @Inject constructor(
  layout: ProjectLayout,
  objects: ObjectFactory,
) : ExtensionAware, WithDefaults<ConventionExtension> {

  /**
   * The target Java version for the project.
   */
  public val jvmTarget: Property<JavaVersion> =
    objects.property(JavaVersion::class.java).convention(JavaVersion.VERSION_17)

  /**
   * The directory for the project's configuration files.
   */
  public val configsDir: DirectoryProperty =
    objects.directoryProperty()
      .convention(layout.projectDirectory.dir(DEFAULT_CONFIGS_DIR))

  /**
   * The directory for the project's report files.
   */
  public val reportsDir: DirectoryProperty =
    objects.directoryProperty()
      .convention(layout.buildDirectory.dir(DEFAULT_REPORTS_DIR))

  /**
   * Sets the default values for the properties.
   *
   * @param defaults The convention extension with the default values.
   */
  override fun setDefaults(defaults: ConventionExtension) {
    jvmTarget.convention(defaults.jvmTarget)
    configsDir.convention(defaults.configsDir)
    reportsDir.convention(defaults.reportsDir)
  }

  /**
   * This is a companion object that contains constants related to the ConventionExtension class.
   *
   * @property NAME The name of the convention extension.
   * @property DEFAULT_CONFIGS_DIR The default directory for the project's configuration files.
   * @property DEFAULT_REPORTS_DIR The default directory for the project's report files.
   */
  public companion object {
    internal const val NAME: String = "convention"
    internal const val DEFAULT_CONFIGS_DIR = "config/"
    internal const val DEFAULT_REPORTS_DIR = "reports/"
  }
}

/**
 * This is an extension property for the ExtensionContainer class.
 * It provides a way to retrieve the convention extension from the extension container.
 *
 * The property uses the findExtByName function to find the convention extension by its name.
 * If the convention extension exists, it is returned. Otherwise, null is returned.
 *
 * @return The convention extension if it exists, or null if it does not.
 */
public val ExtensionContainer.convention: ConventionExtension?
  get() = findExtByName(ConventionExtension.NAME)
