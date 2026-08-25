package convention.icons.taks.internal

import convention.icons.model.IconConfig

internal object IconLibraryClassifier {

  fun groupByLibrary(config: Map<String, List<IconConfig>>): Map<String, Set<String>> {
    val libraryMap = mutableMapOf<String, MutableSet<String>>()

    config.forEach { (iconName, iconConfigs) ->
      iconConfigs.forEach { iconConfig ->
        libraryMap.getOrPut(iconConfig.libraryId) { mutableSetOf() }.add(iconName)
      }
    }

    return libraryMap
  }
}
