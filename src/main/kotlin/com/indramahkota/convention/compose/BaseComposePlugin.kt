package com.indramahkota.convention.compose

import com.indramahkota.convention.common.BaseConventionPlugin
import com.indramahkota.convention.common.annotation.InternalPluginApi
import com.indramahkota.convention.compose.internal.configureCompose
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty

/**
 * This abstract class represents the Base Compose Plugin.
 * It extends the BaseConventionPlugin class and is used to configure the project.
 *
 * @constructor Creates an instance of the BaseComposePlugin class.
 */
public abstract class BaseComposePlugin : BaseConventionPlugin() {

  /**
   * This is a getter for the reportsDir property.
   * It retrieves the DirectoryProperty instance from the conventionExtension's reportsDir.
   *
   * @return The DirectoryProperty instance.
   */
  private val reportsDir: DirectoryProperty
    get() = conventionExtension.reportsDir

  /**
   * This function is used to configure the common Compose Android settings for the project.
   * It configures the Compose settings using the reportsDir property.
   *
   * @receiver The Project instance on which the function is invoked.
   */
  @InternalPluginApi
  protected fun Project.configureCommonComposeAndroid() {
    configureCompose(reportsDir = reportsDir)
  }
}
