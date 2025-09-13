package convention.android.internal

import java.io.File
import org.gradle.api.Project

internal const val PROGUARD_FILENAME = "proguard-android-optimize.txt"

// Collects proguard rules from 'proguard' dir.
internal fun Project.projectProguardFiles(): List<File> {
  return fileTree("proguard").files.filter { it.extension == "pro" }
}
