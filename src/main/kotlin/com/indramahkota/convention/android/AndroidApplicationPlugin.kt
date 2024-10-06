package com.indramahkota.convention.android

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.google.android.libraries.mapsplatform.secrets_gradle_plugin.SecretsPluginExtension
import com.indramahkota.convention.android.dsl.BUILD_TYPE_DEBUG
import com.indramahkota.convention.android.dsl.BUILD_TYPE_QA
import com.indramahkota.convention.android.dsl.BUILD_TYPE_RELEASE
import com.indramahkota.convention.android.dsl.BUILD_TYPE_STAGING
import com.indramahkota.convention.android.dsl.BuildTypeSuffix
import com.indramahkota.convention.android.internal.PROGUARD_FILENAME
import com.indramahkota.convention.android.internal.androidComponents
import com.indramahkota.convention.android.internal.loadKeystoreConfig
import com.indramahkota.convention.android.internal.projectProguardFiles
import com.indramahkota.convention.android.task.MakeDebuggableTask
import com.indramahkota.convention.android.task.RenameApkTask
import com.indramahkota.convention.android.task.RenameBundleTask
import com.indramahkota.convention.common.annotation.InternalPluginApi
import com.indramahkota.convention.common.constant.PLUGIN_ID_ANDROID_APPLICATION
import com.indramahkota.convention.common.constant.SECRET_GRADLEW_PLUGIN
import com.indramahkota.convention.common.internal.applyPlugins
import com.indramahkota.convention.common.internal.requiredPlugin
import com.indramahkota.convention.common.utils.loadPropertiesFile
import net.pearx.kasechange.toPascalCase
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import javax.inject.Inject

/**
 * This class represents the Android Application Plugin.
 * It extends the BaseAndroidPlugin class and is used to configure the project.
 *
 * @property pluginRegistry The registry of plugins used in the project. This is injected in the constructor.
 * @constructor Creates an instance of the AndroidApplicationPlugin class.
 */
public class AndroidApplicationPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseAndroidPlugin() {

  /**
   * This function is used to configure the project.
   * It checks for the required plugins and applies them to the project.
   * It also configures the common Android settings, application Android settings, secrets, and finalizes the application Android settings.
   */
  @InternalPluginApi
  override fun Project.configure() {
    pluginRegistry.requiredPlugin(
      pluginId = PLUGIN_ID_ANDROID_APPLICATION,
      errorMessage = "Android Application Gradle Plugin not found.",
    )
    pluginRegistry.requiredPlugin(
      pluginId = SECRET_GRADLEW_PLUGIN,
      errorMessage = "Secret Gradle Plugin not found.",
    )

    applyPlugins(PLUGIN_ID_ANDROID_APPLICATION, SECRET_GRADLEW_PLUGIN)

    configureCommonAndroid()
    configureApplicationAndroid(androidOptionsExtension)
    configureSecrets()
    finalizeApplicationAndroid()
  }
}

private fun Project.configureApplicationAndroid(androidOptions: AndroidOptionsExtension) =
  extensions.configure<ApplicationExtension> {
    val debugSignName = "DebugSignConfig"
    val releaseSignName = "ReleaseSignConfig"

    val properties = kotlin.runCatching {
      loadPropertiesFile("../keystore.properties")
    }.getOrElse {
      loadPropertiesFile("../keystore.defaults.properties")
    }

    val debugKeystoreConfig = properties.loadKeystoreConfig("DEBUG")
    val releaseKeystoreConfig = properties.loadKeystoreConfig("RELEASE")

    signingConfigs {
      create(debugSignName) {
        keyAlias = debugKeystoreConfig.keyAlias
        keyPassword = debugKeystoreConfig.keyPassword
        storeFile = file(debugKeystoreConfig.storeFile)
        storePassword = debugKeystoreConfig.storePassword
      }
      create(releaseSignName) {
        keyAlias = releaseKeystoreConfig.keyAlias
        keyPassword = releaseKeystoreConfig.keyPassword
        storeFile = file(releaseKeystoreConfig.storeFile)
        storePassword = releaseKeystoreConfig.storePassword
      }
    }

    defaultConfig {
      targetSdk = androidOptions.targetSdk.get()
      // TODO: Create extension for setup resource configurations
      // Fix: getString incorrect value after isShrinkResources=true
      resourceConfigurations.addAll(listOf("en", "in"))
      // Collect proguard rules from 'proguard' dir
      setProguardFiles(projectProguardFiles() + getDefaultProguardFile(PROGUARD_FILENAME))
    }

    buildFeatures {
      buildConfig = true
    }

    dependenciesInfo {
      includeInBundle = false
      includeInApk = false
    }

    buildTypes {
      debug {
        isDebuggable = true
        applicationIdSuffix = BuildTypeSuffix.DEBUG.suffix
        versionNameSuffix = "-".plus(BUILD_TYPE_DEBUG.uppercase())
        signingConfig = signingConfigs.findByName(debugSignName)
      }

      release {
        isDebuggable = false
        isMinifyEnabled = true
        isShrinkResources = true
        applicationIdSuffix = BuildTypeSuffix.RELEASE.suffix
        signingConfig = signingConfigs.findByName(releaseSignName)
      }

      // Development build with performance optimized
      register(BUILD_TYPE_QA) {
        initWith(getByName(BUILD_TYPE_RELEASE))
        applicationIdSuffix = BuildTypeSuffix.QA.suffix
        matchingFallbacks += listOf(BUILD_TYPE_DEBUG, BUILD_TYPE_RELEASE)
        signingConfig = signingConfigs.findByName(debugSignName)

        // We can not use isDebuggable = true here, so set DEBUG field ourselves.
        // See `makeDebuggable` for more information
        buildConfigField(type = "boolean", name = "DEBUG", value = "true")
      }

      // Production build with release app id, but able to debug
      register(BUILD_TYPE_STAGING) {
        initWith(getByName(BUILD_TYPE_RELEASE))
        applicationIdSuffix = BuildTypeSuffix.STAGING.suffix
        matchingFallbacks += listOf(BUILD_TYPE_DEBUG, BUILD_TYPE_RELEASE)

        // We can not use isDebuggable = true here, so set DEBUG field ourselves.
        // See `makeDebuggable` for more information
        buildConfigField(type = "boolean", name = "DEBUG", value = "true")
      }
    }

    packaging {
      // Due to https://github.com/Kotlin/kotlinx.coroutines/issues/2023
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
  }

private fun Project.configureSecrets() {
  extensions.configure<SecretsPluginExtension> {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "secrets.defaults.properties"
  }
}

// @see: https://developer.android.com/reference/tools/gradle-api/7.0/com/android/build/api/extension/ApplicationAndroidComponentsExtension
private fun Project.finalizeApplicationAndroid() =
  androidComponents<ApplicationAndroidComponentsExtension> {
    onVariants { variant ->
      val loader = variant.artifacts.getBuiltArtifactsLoader()
      val apk = variant.artifacts.get(SingleArtifact.APK)
      val bundle = variant.artifacts.get(SingleArtifact.BUNDLE)
      val manifest = variant.artifacts.get(SingleArtifact.MERGED_MANIFEST)

      tasks.register(
        "make${name.toPascalCase()}${variant.name.toPascalCase()}RenamedApk",
        RenameApkTask::class.java,
      ) {
        group = "Build Logic"
        description =
          "Rename project: ${this@finalizeApplicationAndroid.name.toPascalCase()} for variant: ${variant.name.toPascalCase()} APK"
        mBuiltArtifactsLoader.set(loader)
        inputApkDirectory.set(apk)
        inputManifestDirectory.set(manifest)
        variantName.set(variant.name)
      }

      tasks.register(
        "make${name.toPascalCase()}${variant.name.toPascalCase()}RenamedBundle",
        RenameBundleTask::class.java,
      ) {
        group = "Build Logic"
        description =
          "Rename project: ${this@finalizeApplicationAndroid.name.toPascalCase()} for variant: ${variant.name.toPascalCase()} Bundle"
        inputBundleDirectory.set(bundle)
        inputManifestDirectory.set(manifest)
        variantName.set(variant.name)
      }
    }

    // Each build variant is typically defined by a combination of Build types and Product flavors
    // Ex: freeStaging, paidStaging, where free and paid is Product flavors
    onVariants(selector().withBuildType(BUILD_TYPE_STAGING)) { variant ->
      val makeDebuggableTask = tasks.register<MakeDebuggableTask>(
        "make${name.toPascalCase()}${variant.name.toPascalCase()}Debuggable",
      ) {
        group = "Build Logic"
        description =
          "Make project: ${this@finalizeApplicationAndroid.name.toPascalCase()} for variant: ${variant.name.toPascalCase()} APK debuggable"
      }

      variant.artifacts.use(makeDebuggableTask)
        .wiredWithFiles(taskInput = { it.mergedManifest }, taskOutput = { it.debuggableManifest })
        .toTransform(SingleArtifact.MERGED_MANIFEST)
    }
  }
