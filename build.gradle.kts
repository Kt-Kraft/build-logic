import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  `maven-publish`
  alias(libs.plugins.ben.manes.versions)
  alias(libs.plugins.version.catalog.update)
  alias(libs.plugins.dokka)
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
  implementation(libs.android.application.gradle.plugin)
  implementation(libs.android.library.gradle.plugin)
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.kotlin.android.gradle.plugin)
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
    freeCompilerArgs.addAll(
      listOf(
        "-opt-in=kotlin.Experimental",
        "-opt-in=kotlin.RequiresOptIn",
      ),
    )
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
  }
}

/**
 * -----------------------------------
 * MAVEN PUBLICATION
 * -----------------------------------
 * */
group = "convention"
version = "1.1.0"

afterEvaluate {
  publishing {
    publications.withType<MavenPublication>().all {
      pom {
        licenses {
          license {
            name.set("MIT License")
            url.set("https://github.com/Kt-Kraft/build-logic/blob/main/LICENSE")
            distribution.set("repo")
          }
        }
        developers {
          developer {
            id.set("indramahkota")
            name.set("Indra Mahkota")
            email.set("indramahkota1@gmail.com")
          }
        }
        issueManagement {
          url.set("https://github.com/Kt-Kraft/build-logic/issues")
          system.set("GitHub Issues")
        }
        scm {
          url.set("https://github.com/Kt-Kraft/build-logic/")
          connection.set("scm:git:git://github.com:Kt-Kraft/build-logic.git")
          developerConnection.set("scm:git:ssh://git@github.com:Kt-Kraft/build-logic.git")
        }
      }
    }

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
}
