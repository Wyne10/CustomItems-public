package org.bigcraft.customitems.spi

import me.wyne.wutils.config.configurables.attribute.GenericFactoryMap
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.api.spi.ActivatorAttribute
import org.bigcraft.customitems.api.spi.ActivatorCondition
import org.bigcraft.customitems.api.spi.ConfigFactory
import org.bigcraft.customitems.api.spi.CustomItemStackAttribute
import org.bigcraft.customitems.api.spi.CustomItemsRegistries
import org.bigcraft.customitems.api.spi.FactoryRegistry
import org.bigcraft.customitems.api.spi.InstanceRegistry
import org.bigcraft.customitems.api.spi.ItemIdentifier
import org.bigcraft.customitems.api.spi.KeyedExtension
import org.bukkit.configuration.ConfigurationSection

/**
 * The plugin's extension points.
 *
 * Registration is open from the moment the plugin enables until items are first loaded, which
 * happens once the server has finished enabling plugins — long enough for any plugin depending on
 * CustomItems to register from its own `onEnable`.
 */
object Registries : CustomItemsRegistries {

    private var open = true

    val activatorAttributeFactories = KeyedFactoryRegistry<ActivatorAttribute>("activator attribute")
    val activatorConditionFactories = KeyedFactoryRegistry<ActivatorCondition>("activator condition")
    val itemAttributeFactories = ItemAttributeRegistry()
    val itemIdentifierInstances = KeyedInstanceRegistry<ItemIdentifier>("item identifier")

    override fun activatorAttributes(): FactoryRegistry<ActivatorAttribute> = activatorAttributeFactories

    override fun activatorConditions(): FactoryRegistry<ActivatorCondition> = activatorConditionFactories

    override fun itemAttributes(): FactoryRegistry<CustomItemStackAttribute> = itemAttributeFactories

    override fun itemIdentifiers(): InstanceRegistry<ItemIdentifier> = itemIdentifierInstances

    override fun isOpen(): Boolean = open

    fun close() {
        open = false
    }

    internal fun rejectIfClosed(vocabulary: String, key: String): Boolean {
        if (open) return false
        CustomItems.logger.warn(
            "Ignoring {} '{}' registered after items were loaded; register from onEnable instead",
            vocabulary, key
        )
        return true
    }

}

/** A [FactoryRegistry] backed by WUtils' key resolution, including `attributeType` aliases. */
class KeyedFactoryRegistry<T : Any>(private val vocabulary: String) : FactoryRegistry<T> {

    private val factories = GenericFactoryMap<T>()

    override fun register(key: String, factory: ConfigFactory<out T>) {
        if (Registries.rejectIfClosed(vocabulary, key)) return
        factories.put(key) { resolvedKey, config -> factory.create(resolvedKey, config) }
    }

    override fun contains(key: String): Boolean = factories.keyMap.containsKey(key)

    override fun keys(): Set<String> = factories.keyMap.keys

    fun createAll(config: ConfigurationSection): Set<T> = factories.createAll(config)

    fun createAllMap(config: ConfigurationSection): Map<String, T> = factories.createAllMap(config)

}

/** An [InstanceRegistry] over shared, stateless extensions. */
class KeyedInstanceRegistry<T : KeyedExtension>(private val vocabulary: String) : InstanceRegistry<T> {

    private val instances = LinkedHashMap<String, T>()

    override fun register(extension: T) {
        if (Registries.rejectIfClosed(vocabulary, extension.key)) return
        instances[extension.key] = extension
    }

    override fun get(key: String): T? = instances[key]

    override fun contains(key: String): Boolean = instances.containsKey(key)

    override fun keys(): Set<String> = instances.keys

}
