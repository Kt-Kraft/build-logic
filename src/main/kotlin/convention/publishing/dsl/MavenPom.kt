package convention.publishing.dsl

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
  var owner: String,
  var repository: String,
) {
  val baseUrl: String get() = "https://$GITHUB_DOMAIN/$owner/$repository"
  val issuesUrl: String get() = "$baseUrl/issues"

  val scmGit: String get() = "scm:git:git://$GITHUB_DOMAIN/$owner/$repository.git"
  val scmSsh: String get() = "scm:git:ssh://git@$GITHUB_DOMAIN:$owner/$repository.git"

  public companion object {
    private const val GITHUB_DOMAIN = "github.com"
  }
}

public fun MavenPom.setGitHubProject(
  owner: String,
  repository: String,
  action: GithubProject.() -> Unit = {},
) {
  val project = GithubProject(owner, repository).apply(action)

  url.set(project.baseUrl)

  issueManagement {
    url.set(project.issuesUrl)
    system.set("GitHub Issues")
  }

  scm {
    url.set(project.baseUrl)
    connection.set(project.scmGit)
    developerConnection.set(project.scmSsh)
  }
}

public fun MavenPomDeveloperSpec.developer(
  id: String,
  name: String,
  email: String,
  action: MavenPomDeveloper.() -> Unit = {},
): Unit = developer {
  this.id.set(id)
  this.name.set(name)
  this.email.set(email)
  action()
}

public fun MavenPomContributorSpec.contributor(
  name: String,
  email: String,
  action: MavenPomContributor.() -> Unit = {},
): Unit = contributor {
  this.name.set(name)
  this.email.set(email)
  action()
}
