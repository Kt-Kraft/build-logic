package com.indramahkota.convention.compose.internal

import com.android.build.api.dsl.CommonExtension
import com.indramahkota.convention.android.internal.android
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun Project.configureCompose(
  reportsDir: DirectoryProperty,
) {
  android<CommonExtension<*, *, *, *, *, *>> {
    buildFeatures {
      compose = true
    }
  }

  extensions.configure<ComposeCompilerGradlePluginExtension> {
    enableStrongSkippingMode.set(true)
    reportsDestination.set(reportsDir.dir("compose-reports"))
    metricsDestination.set(reportsDir.dir("compose-metrics"))
  }
}
