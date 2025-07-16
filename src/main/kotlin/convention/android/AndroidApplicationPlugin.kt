package convention.android

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.google.android.libraries.mapsplatform.secrets_gradle_plugin.SecretsPluginExtension
import convention.android.dsl.BUILD_TYPE_DEBUG
import convention.android.dsl.BUILD_TYPE_PROFILE
import convention.android.dsl.BUILD_TYPE_RELEASE
import convention.android.dsl.BuildTypeSuffix
import convention.android.internal.PROGUARD_FILENAME
import convention.android.internal.androidComponents
import convention.android.internal.loadKeystoreConfig
import convention.android.internal.projectProguardFiles
import convention.android.task.MakeDebuggableTask
import convention.android.task.RenameApkTask
import convention.android.task.RenameBundleTask
import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_ANDROID_APPLICATION
import convention.common.constant.SECRET_GRADLEW_PLUGIN
import convention.common.internal.applyPlugins
import convention.common.internal.requiredPlugin
import convention.common.utils.loadPropertiesFile
import javax.inject.Inject
import net.pearx.kasechange.toPascalCase
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

public class AndroidApplicationPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseAndroidPlugin() {

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

    configureKotlinAndroid()
    configureCommonAndroid()
    configureApplicationAndroid(androidOptionsExtension)
    finalizeApplicationAndroid()
    configureSecrets()
  }
}

private fun Project.configureApplicationAndroid(androidOptions: AndroidOptionsExtension) =
  extensions.configure<ApplicationExtension> {
    val debugSignName = "DebugSignConfig"
    val releaseSignName = "ReleaseSignConfig"

    val properties = runCatching {
      loadPropertiesFile(
        rootDir.resolve("keystore.properties")
      )
    }.getOrElse {
      loadPropertiesFile(
        rootDir.resolve("keystore.defaults.properties")
      )
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
      // Collect proguard rules from 'proguard' dir
      setProguardFiles(projectProguardFiles() + getDefaultProguardFile(PROGUARD_FILENAME))
    }

    androidResources {
      // TODO: Create extension for setup resource configurations
      // Fix: getString incorrect value after isShrinkResources=true
      localeFilters.addAll(listOf("en", "in"))
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

      // Production build with release app id, but able to debug
      register(BUILD_TYPE_PROFILE) {
        initWith(getByName(BUILD_TYPE_RELEASE))
        matchingFallbacks += listOf(BUILD_TYPE_RELEASE)

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
      val stringsXml = layout.projectDirectory.asFile.resolve("src/main/res/values/strings.xml")

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
        inputStringsDirectory.set(stringsXml)
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
        inputStringsDirectory.set(stringsXml)
        variantName.set(variant.name)
      }
    }

    // Each build variant is typically defined by a combination of Build types and Product flavors
    // Ex: freeStaging, paidStaging, where free and paid is Product flavors
    onVariants(selector().withBuildType(BUILD_TYPE_PROFILE)) { variant ->
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
