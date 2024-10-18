<h1 align="center">Build Logic for Androidd</h1>

<div align="center">
<a href="https://github.com/Kt-Kraft/build-logic/blob/master/LICENSE"><img src="https://img.shields.io/github/license/Kt-Kraft/build-logic?color=blue" alt="LICENSE"/></a> <a href="https://github.com/Kt-Kraft/build-logic/stargazers"><img src="https://img.shields.io/github/stars/Kt-Kraft/build-logic" alt="GitHub Stars"/></a> <a href="#contributors"><img src="https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat" alt="All Contributors"/></a>
</div>

<br/>

## 📝 Usage

```kt
// Root project settings.gradle.kts
pluginManagement {
  repositories {
    maven(url = "https://maven.pkg.github.com/indramahkota/build-logic/") {
      name = "GitHubPackages"
      credentials {
        username = providers.gradleProperty("github.username").orNull
          ?: System.getenv("GITHUB_USERNAME")
        password = providers.gradleProperty("github.token").orNull
          ?: System.getenv("GITHUB_TOKEN")
      }
    }
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    maven(url = "https://maven.pkg.github.com/indramahkota/version-catalog/") {
      credentials {
        username = providers.gradleProperty("github.username").orNull
          ?: System.getenv("GITHUB_USERNAME")
        password = providers.gradleProperty("github.token").orNull
          ?: System.getenv("GITHUB_TOKEN")
      }
    }
    google()
    mavenCentral()
  }
}
```

```kt
// Root project build.gradle.kts
plugins {
  alias(libs.plugins.convention.android.app) apply false
  alias(libs.plugins.convention.android.lib) apply false
  alias(libs.plugins.convention.compose.app) apply false
  alias(libs.plugins.convention.compose.lib) apply false
  alias(libs.plugins.convention.publishing) apply false
  alias(libs.plugins.convention.android.config)
  alias(libs.plugins.convention.publish.config)
}

// Initial configuration for subprojects
convention {
  // Set android config for all subprojects
  android {
    minSdk.set(23)
    targetSdk.set(34)
    compileSdk.set(34)
  }

  // Set maven pom for all subprojects
  publishing {
    pom {
      setGitHubProject {
        owner = "indramahkota"
        repository = "easy-android"
      }

      licenses {
        mit()
      }

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

```

```kt
// In submodules project build.gradle.kts
plugins {
  // Automatically apply android plugin
  alias(libs.plugins.convention.compose.app)
  alias(libs.plugins.secret.gradle.plugin)
}

//or

plugins {
  // Automatically apply android plugin
  alias(libs.plugins.convention.android.lib)
  alias(libs.plugins.convention.publishing)
}

```
