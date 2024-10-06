package com.indramahkota.convention.publishing

import com.indramahkota.convention.common.BaseConventionPlugin
import com.indramahkota.convention.common.annotation.InternalPluginApi
import org.gradle.api.Project

public open class PublishConfigPlugin : BaseConventionPlugin() {

  @InternalPluginApi
  override fun Project.configure() {
    createExtension(
      name = PublishingOptionsExtension.NAME,
      publicType = PublishingOptionsExtension::class,
    )
  }
}
