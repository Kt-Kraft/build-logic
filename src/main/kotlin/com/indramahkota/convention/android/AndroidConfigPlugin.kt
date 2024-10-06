package com.indramahkota.convention.android

import com.indramahkota.convention.common.BaseConventionPlugin
import com.indramahkota.convention.common.annotation.InternalPluginApi
import org.gradle.api.Project

/**
 * This class represents the Android Configuration Plugin.
 * It extends the BaseConventionPlugin class and is used to configure the project.
 *
 * @constructor Creates an instance of the AndroidConfigPlugin class.
 */
public open class AndroidConfigPlugin : BaseConventionPlugin() {

  /**
   * This function is used to configure the project.
   * It creates an extension with the name and public type specified in the AndroidOptionsExtension class.
   */
  @InternalPluginApi
  override fun Project.configure() {
    createExtension(
      name = AndroidOptionsExtension.NAME,
      publicType = AndroidOptionsExtension::class,
    )
  }
}
