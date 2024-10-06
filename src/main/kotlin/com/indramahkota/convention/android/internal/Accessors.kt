package com.indramahkota.convention.android.internal

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project

// For setup CommonExtension, LibraryExtension, and ApplicationExtension
internal fun <T : CommonExtension<*, *, *, *, *, *>> Project.android(configure: T.() -> Unit) {
  extensions.configure("android", configure)
}

// For setup api or running tasks on specific variant
internal fun <T : AndroidComponentsExtension<*, *, *>> Project.androidComponents(configure: T.() -> Unit) {
  extensions.configure("androidComponents", configure)
}
