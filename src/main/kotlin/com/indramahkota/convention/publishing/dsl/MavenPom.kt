package com.indramahkota.convention.publishing.dsl

import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomContributor
import org.gradle.api.publish.maven.MavenPomContributorSpec
import org.gradle.api.publish.maven.MavenPomDeveloper
import org.gradle.api.publish.maven.MavenPomDeveloperSpec
import org.gradle.api.publish.maven.MavenPomLicenseSpec

public fun MavenPomLicenseSpec.mit() {
  license {
    name.set("MIT License")
    url.set("https://opensource.org/licenses/mit-license.php")
  }
}

public data class GithubProject(
  var owner: String? = null,
  var repository: String? = null,
) {
  val url: String
    get() = "$HTTPS$GITHUB_DOMAIN/$owner/$repository"

  val ssh: String
    get() = "$SSH$GITHUB_DOMAIN:$owner/$repository.git"

  val git: String
    get() = "$GIT$GITHUB_DOMAIN/$owner/$repository.git"

  private companion object {
    const val GIT = "scm:git:git://"
    const val SSH = "scm:git:ssh://git@"
    const val HTTPS = "https://"
    const val GITHUB_DOMAIN = "github.com"
  }
}

public fun MavenPom.setGitHubProject(
  action: GithubProject.() -> Unit = {},
) {
  val githubProject = GithubProject().apply {
    action()
  }

  require(!githubProject.owner.isNullOrEmpty()) {
    "GitHub project owner must be set"
  }

  require(!githubProject.repository.isNullOrEmpty()) {
    "GitHub project repository must be set"
  }

  url.set(githubProject.url)

  issueManagement {
    url.set("${githubProject.url}/issues")
    system.set("GitHub Issues")
  }

  scm {
    url.set(githubProject.url)
    connection.set(githubProject.git)
    developerConnection.set(githubProject.ssh)
  }
}

public fun MavenPomDeveloperSpec.developer(
  id: String,
  name: String,
  email: String,
  action: MavenPomDeveloper.() -> Unit = {},
) {
  developer {
    this.id.set(id)
    this.name.set(name)
    this.email.set(email)
    action()
  }
}

public fun MavenPomContributorSpec.contributor(
  name: String,
  email: String,
  action: MavenPomContributor.() -> Unit = {},
) {
  contributor {
    this.name.set(name)
    this.email.set(email)
    action()
  }
}
