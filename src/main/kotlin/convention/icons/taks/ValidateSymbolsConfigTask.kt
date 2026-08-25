package convention.icons.taks

import convention.icons.plugin.SymbolCraftExtension
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Validation only reports on the configuration and produces no output")
public abstract class ValidateSymbolsConfigTask : DefaultTask() {
  @get:Internal
  public abstract val extension: Property<SymbolCraftExtension>

  @TaskAction
  public fun validate() {
    val config = extension.get().getIconsConfig()
    if (config.isEmpty()) {
      throw IllegalStateException(
        "No icons configured. Use symbolCraft { } in build.gradle.kts"
      )
    }
    val count = config.values.sumOf { it.size }
    logger.lifecycle(
      "✅ Valid configuration. Icons: ${config.size}, Total configurations: $count"
    )
  }
}
