import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import convention.publishing.dsl.developer
import convention.publishing.dsl.mit
import convention.publishing.dsl.setGitHubProject
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  `maven-publish`
  signing
  alias(libs.plugins.ben.manes.versions)
  alias(libs.plugins.version.catalog.update)
  alias(libs.plugins.dokka)
  alias(libs.plugins.convention.publish.config)
  alias(libs.plugins.convention.publishing)
  alias(libs.plugins.convention.commitlint)
}

// https://kotlinlang.org/docs/gradle-configure-project.html#apply-the-plugin
// Min - Max without deprecated warning
// Kotlin version   | Gradle Version    | AGP version
// 2.0.20           | 6.8.3 – 8.8       | 7.1.3 – 8.5.2

// https://developer.android.com/build/releases/gradle-plugin
// AGP version      |	Minimum required Gradle version
// 8.5              |	8.7

dependencies {
  implementation(libs.android.r8)
  implementation(libs.android.tools.build)
  implementation(libs.android.tools.common)
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.compose.compiler.gradle.plugin)
  implementation(libs.jgit)
  implementation(libs.google.guava)
  implementation(libs.secret.gradle.plugin)
  implementation(libs.kase.change)
  testImplementation(kotlin("test"))
}

kotlin {
  explicitApi()
  jvmToolchain(17)
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

tasks.test {
  useJUnitPlatform()
}

fun isNonStable(version: String): Boolean {
  val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isStable = stableKeyword || regex.matches(version)
  return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
  rejectVersionIf {
    isNonStable(candidate.version)
  }
}

versionCatalogUpdate {
  sortByKey.set(true)
  keep {
    keepUnusedVersions.set(true)
    keepUnusedLibraries.set(true)
    keepUnusedPlugins.set(true)
  }
}

gradlePlugin {
  plugins {
    // Android
    create("android-config") {
      id = "android.config"
      displayName = "Android Config Plugin"
      description = displayName
      implementationClass = "convention.android.AndroidConfigPlugin"
    }
    create("android-app") {
      id = "android.app"
      displayName = "Android Application Plugin"
      description = displayName
      implementationClass = "convention.android.AndroidApplicationPlugin"
    }
    create("android-library") {
      id = "android.lib"
      displayName = "Android Library Plugin"
      description = displayName
      implementationClass = "convention.android.AndroidLibraryPlugin"
    }

    // Compose
    register("compose-app") {
      id = "compose.app"
      displayName = "Compose Application Plugin"
      description = displayName
      implementationClass = "convention.compose.ComposeApplicationPlugin"
    }
    register("compose-library") {
      id = "compose.lib"
      displayName = "Compose Library Plugin"
      description = displayName
      implementationClass = "convention.compose.ComposeLibraryPlugin"
    }

    // Multiplatform
    create("multiplatform-config") {
      id = "multiplatform.config"
      displayName = "Multiplatform Config Plugin"
      description = displayName
      implementationClass = "convention.multiplatform.MultiplatformConfigPlugin"
    }
    create("multiplatform") {
      id = "multiplatform"
      displayName = "Multiplatform Plugin"
      description = displayName
      implementationClass = "convention.multiplatform.MultiplatformPlugin"
    }

    // Publishing
    create("publish-config") {
      id = "publish.config"
      displayName = "Publish Config Plugin"
      description = displayName
      implementationClass = "convention.publishing.PublishConfigPlugin"
    }
    create("publish") {
      id = "publish"
      displayName = "Publishing Plugin"
      description = displayName
      implementationClass = "convention.publishing.PublishPlugin"
    }

    // CommitLint
    create("commit-lint") {
      id = "commitlint"
      displayName = "CommitLint Plugin"
      description = displayName
      implementationClass = "convention.commitlint.CommitLintPlugin"
    }
  }
}

/**
 * -----------------------------------
 * MAVEN PUBLICATION
 * -----------------------------------
 * */
group = "convention"
version = "1.3.0"

convention {
  publishing {
    pom {
      setGitHubProject {
        owner = "Kt-Kraft"
        repository = "build-logic"
      }

      licenses { mit() }

      developers {
        developer(
          id = "indramahkota",
          name = "Indra Mahkota",
          email = "indramahkota1@gmail.com",
        )
      }
    }
  }
}

afterEvaluate {
  publishing {
    repositories {
      maven(url = "https://maven.pkg.github.com/Kt-Kraft/build-logic/") {
        name = "GitHubPackages"
        credentials {
          username = System.getenv("GITHUB_USERNAME")
          password = System.getenv("GITHUB_TOKEN")
        }
      }
    }
  }

  signing {
    useGpgCmd()
    sign(publishing.publications)
  }
}
