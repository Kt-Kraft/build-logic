package convention.publishing

import com.android.build.api.dsl.LibraryExtension
import convention.common.BaseConventionPlugin
import convention.common.annotation.InternalPluginApi
import convention.common.constant.PLUGIN_ID_ANDROID_LIBRARY
import convention.common.constant.PLUGIN_ID_JAVA
import convention.common.constant.PLUGIN_ID_JAVA_GRADLE_PLUGIN
import convention.common.constant.PLUGIN_ID_MAVEN_PUBLISH
import convention.common.constant.PLUGIN_ID_VERSION_CATALOG
import convention.common.internal.hasPlugin
import convention.publishing.internal.isPluginAutomatedPublishing
import convention.publishing.internal.java
import convention.publishing.internal.publishing
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginRegistry
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName

public open class PublishPlugin @Inject constructor(
  private val pluginRegistry: PluginRegistry,
) : BaseConventionPlugin() {

  private val publishingOptionsExtension: PublishingOptionsExtension
    get() = conventionOptions.extensions.publishingOptions

  @InternalPluginApi
  override fun Project.configure() {
    check(pluginRegistry.hasPlugin(PLUGIN_ID_MAVEN_PUBLISH)) {
      "Maven Publish Gradle Plugin not found."
    }
    pluginManager.apply(PLUGIN_ID_MAVEN_PUBLISH)
    val publicationName = when {
      plugins.hasPlugin(PLUGIN_ID_ANDROID_LIBRARY) -> configureAndroidLibraryPublication()
      plugins.hasPlugin(PLUGIN_ID_JAVA_GRADLE_PLUGIN) && isPluginAutomatedPublishing -> configurePluginPublication()
      plugins.hasPlugin(PLUGIN_ID_VERSION_CATALOG) -> configureVersionCatalogPublication()
      plugins.hasPlugin(PLUGIN_ID_JAVA) -> configureJavaLibraryPublication()
      else -> error("Project type has not recognized ${project.name}.")
    }
    afterEvaluate {
      publishing.publications.getByName<MavenPublication>(publicationName) {
        pom {
          name.convention(project.name)
          description.convention(project.description)
          publishingOptionsExtension.configurePom.get().invoke(this)
        }
      }
    }
  }

  private fun Project.configureAndroidLibraryPublication(): String {
    extensions.configure<LibraryExtension>("android") {
      publishing {
        singleVariant("release")
        // Disable sources and javadoc jars for now
        /*{
          withSourcesJar()
          withJavadocJar()
        }*/
      }
    }
    publishing {
      publications.create<MavenPublication>(PUBLICATION_NAME) {
        afterEvaluate {
          from(components["release"])
        }
      }
    }
    return PUBLICATION_NAME
  }

  private fun Project.configurePluginPublication(): String {
    java {
      withSourcesJar()
      withJavadocJar()
    }
    return PLUGIN_PUBLICATION_NAME
  }

  private fun Project.configureVersionCatalogPublication(): String {
    publishing {
      publications.create<MavenPublication>(PUBLICATION_NAME) {
        from(components["versionCatalog"])
      }
    }
    return PUBLICATION_NAME
  }

  private fun Project.configureJavaLibraryPublication(): String {
    java {
      withSourcesJar()
      withJavadocJar()
    }
    publishing {
      publications.create<MavenPublication>(PUBLICATION_NAME) {
        from(components["java"])
      }
    }
    return PUBLICATION_NAME
  }

  private companion object {
    const val PUBLICATION_NAME: String = "maven"
    const val PLUGIN_PUBLICATION_NAME: String = "pluginMaven"
  }
}
