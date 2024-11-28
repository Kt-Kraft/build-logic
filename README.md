<h1 align="center">Build Logic for Android</h1>

<div align="center">
  <a href="https://github.com/Kt-Kraft/build-logic/blob/master/LICENSE"><img src="https://img.shields.io/github/license/Kt-Kraft/build-logic?color=blue" alt="LICENSE"/></a>
  <a href="https://github.com/Kt-Kraft/build-logic/stargazers"><img src="https://img.shields.io/github/stars/Kt-Kraft/build-logic" alt="GitHub Stars"/></a>
  <a href="#contributors"><img src="https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat" alt="All Contributors"/></a>
</div>

<br/>

<p align="center">
  Simplify your Android project configurations with reusable build logic setups. Ideal for multi-module projects, this setup reduces boilerplate, standardizes configurations, supports commitlint, and streamlines publishing with Gradle.
</p>

<br/>

---

## 🚀 Overview

This repository offers a centralized setup for Android build configurations, including the following conventions:
- **Standardized Build Configurations**: Predefined configurations for App, Library, Jetpack Compose modules.
- **Conventional Commit Linting**: Integrated support for enforcing conventional commit messages.
- **Streamlined Publishing Settings**: Simplified configuration for publishing artifacts to GitHub Packages.

---

## 📝 Setup & Usage

To integrate this build logic into your Android project, follow the steps below.

### Step 1: Configure `settings.gradle.kts`

Add the custom plugin repository to your root project `settings.gradle.kts`:

```kotlin
pluginManagement {
  repositories {
    maven(url = "https://maven.pkg.github.com/Kt-Kraft/build-logic/") {
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
```

### Step 2: Update `build.gradle.kts` in Root Project

Add plugin aliases and configurations in your root project’s `build.gradle.kts`:


```kotlin
plugins {
  alias(libs.plugins.convention.android.app) apply false
  alias(libs.plugins.convention.android.lib) apply false
  alias(libs.plugins.convention.compose.app) apply false
  alias(libs.plugins.convention.compose.lib) apply false
  alias(libs.plugins.convention.publishing) apply false
  alias(libs.plugins.convention.android.config)
  alias(libs.plugins.convention.publish.config)
  alias(libs.plugins.convention.commitlint)
}

// Initial configuration for subprojects
convention {

  android {
    minSdk.set(26)
    targetSdk.set(34)
    compileSdk.set(34)
  }

  publishing {
    pom {
      setGitHubProject {
        owner = "indramahkota"
        repository = "easy-android"
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
```

### Step 3: Apply Plugins in Submodules

In your submodule `build.gradle.kts` files, apply the necessary plugins:


```kotlin
plugins {
  alias(libs.plugins.convention.compose.app)
  alias(libs.plugins.secret.gradle.plugin)
}

// or

plugins {
  alias(libs.plugins.convention.android.lib)
  alias(libs.plugins.convention.publishing)
}
```
