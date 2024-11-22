package convention.common.internal

import org.gradle.api.Project

/**
 * It provides a sequence of the project's parent projects, starting from the immediate parent and going up to the root project.
 *
 * @return A sequence of the project's parent projects, from the immediate parent to the root project.
 */
@PublishedApi
internal val Project.parents: Sequence<Project>
  get() = sequence {
    var project: Project? = this@parents.parent
    while (project != null) {
      yield(project)
      project = project.parent
    }
  }
