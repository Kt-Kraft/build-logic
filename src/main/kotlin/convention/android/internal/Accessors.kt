package convention.android.internal

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project
import com.android.build.api.dsl.CommonExtension as ParameterizedCommonExtension

// AGP 9 dropped the type parameters from CommonExtension.
internal typealias CommonExtension = ParameterizedCommonExtension

// For setup CommonExtension, LibraryExtension, and ApplicationExtension
@JvmName("androidCommon")
internal fun Project.android(configure: CommonExtension.() -> Unit) {
  android<CommonExtension>(configure)
}

internal fun <T : CommonExtension> Project.android(configure: T.() -> Unit) {
  extensions.configure("android", configure)
}

// For setup api or running tasks on specific variant
internal fun <T : AndroidComponentsExtension<*, *, *>> Project.androidComponents(configure: T.() -> Unit) {
  extensions.configure("androidComponents", configure)
}
