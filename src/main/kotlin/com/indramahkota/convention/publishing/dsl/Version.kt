package com.indramahkota.convention.publishing.dsl

import org.gradle.api.Project

private val Project.isSnapshotVersion: Boolean
  get() = version.toString().endsWith("-SNAPSHOT")

internal val Project.isReleaseVersion: Boolean
  get() = !isSnapshotVersion
