package me.wyne.customitems

import me.wyne.wutils.common.loadable.Loadable
import me.wyne.wutils.common.loadable.Loader
import me.wyne.wutils.config.configurables.attribute.GenericFactory
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * Loads keyed values from both the `<sectionKey>` section of the plugin config and the
 * `<dataFolder>/<sectionKey>` directory tree, where each file is one value keyed by its name.
 */
@Suppress("LeakingThis")
open class LoadableProvider<V>(
    protected val sectionKey: String,
    protected val factory: GenericFactory<V>,
) : Loadable {

    protected val loadedMap: MutableMap<String, V> = LinkedHashMap()

    val directory = File(CustomItems.instance.dataFolder, sectionKey)

    val map
        get() = loadedMap.toMap()

    init {
        Loader.global.registerLoadable(this)
    }

    operator fun get(key: String): V? =
        loadedMap[key]

    override fun load(config: ConfigurationSection) {
        loadedMap.clear()
        config.getConfigurationSection(sectionKey)?.let { section ->
            section.getKeys(false).forEach { key -> loadKey(key, section, sectionKey, "") }
        }
        loadDirectory(directory, "")
    }

    /** Hook for values that need to know which subdirectory they came from; [path] is dot-terminated, empty at the root. */
    protected open fun onLoaded(key: String, value: V, path: String) = Unit

    private fun loadDirectory(directory: File, path: String) {
        if (!directory.exists()) {
            directory.mkdirs()
            return
        }
        val files = directory.listFiles() ?: return
        files.filter { it.isDirectory }
            .forEach { loadDirectory(it, "$path${it.name}.") }
        files.filter { it.isFile }
            .forEach { file ->
                loadKey(file.nameWithoutExtension, YamlConfiguration.loadConfiguration(file), "$sectionKey/$path", path)
            }
    }

    private fun loadKey(key: String, config: ConfigurationSection, origin: String, path: String) {
        CustomItems.logger.debug("Loading key '{}' from '{}'", key, origin)
        runCatching { factory.create(key, config) }
            .onSuccess {
                loadedMap[key] = it
                onLoaded(key, it, path)
            }
            .onFailure { CustomItems.logger.error("Failed loading '{}' from '{}'", key, origin, it) }
    }

}
