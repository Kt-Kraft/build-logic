package convention.common.internal

import org.gradle.api.Project

/**
 * This property is an extension property for the Project class.
 * It provides a sequence of the project's parent projects, starting from the immediate parent and going up to the root project.
 *
 * The property is marked with the @PublishedApi annotation, which means it is part of the public API
 * but is internal to the module and not intended to be used outside of it.
 * The usage of this API outside the module can lead to unpredictable results and is generally discouraged.
 *
 * The property uses a sequence builder to generate the sequence of parent projects.
 * Inside the sequence builder, it uses a while loop to iterate through the parent projects.
 * On each iteration, it yields the current parent project and then moves to the next parent project.
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
