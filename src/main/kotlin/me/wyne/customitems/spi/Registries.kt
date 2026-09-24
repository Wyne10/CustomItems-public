package me.wyne.customitems.spi

import me.wyne.wutils.config.configurables.attribute.GenericFactoryMap
import me.wyne.customitems.CustomItems
import me.wyne.customitems.cooldown.CooldownService
import me.wyne.customitems.api.spi.ActivatorAttribute
import me.wyne.customitems.api.spi.ActivatorCondition
import me.wyne.customitems.api.spi.ActivatorType
import me.wyne.customitems.api.spi.BlockAttributeType
import me.wyne.customitems.api.spi.ConfigFactory
import me.wyne.customitems.api.spi.Cooldown
import me.wyne.customitems.api.spi.CooldownFeedback
import me.wyne.customitems.api.spi.CustomItemStackAttribute
import me.wyne.customitems.api.spi.CustomItemsRegistries
import me.wyne.customitems.api.spi.FactoryRegistry
import me.wyne.customitems.api.spi.InstanceRegistry
import me.wyne.customitems.api.spi.ItemIdentifier
import me.wyne.customitems.api.spi.ItemPlaceholderProvider
import me.wyne.customitems.api.spi.RestrictionType
import me.wyne.customitems.api.spi.KeyedExtension
import org.bukkit.configuration.ConfigurationSection
import me.wyne.wutils.common.event.ListenerRegistry
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

/**
 * The plugin's extension points.
 *
 * Registration is open from the moment the plugin enables until items are first loaded, which
 * happens once the server has finished enabling plugins — long enough for any plugin depending on
 * CustomItems to register from its own `onEnable`.
 */
object Registries : CustomItemsRegistries {

    private var open = true

    val activatorTypeInstances = KeyedInstanceRegistry<ActivatorType>("activator type")
    val restrictionTypeInstances = KeyedInstanceRegistry<RestrictionType<*>>("restriction")
    val blockAttributeTypeInstances = KeyedInstanceRegistry<BlockAttributeType<*>>("block attribute")
    val activatorAttributeFactories = KeyedFactoryRegistry<ActivatorAttribute>("activator attribute")
    val activatorConditionFactories = KeyedFactoryRegistry<ActivatorCondition>("activator condition")
    val cooldownFactories = KeyedFactoryRegistry<Cooldown>("cooldown")
    val cooldownFeedbackFactories = KeyedFactoryRegistry<CooldownFeedback>("cooldown feedback")
    val itemAttributeFactories = ItemAttributeRegistry()
    val itemPlaceholderProviders: List<ItemPlaceholderProvider>
        field = mutableListOf<ItemPlaceholderProvider>()
    val itemIdentifierInstances = KeyedInstanceRegistry<ItemIdentifier>("item identifier")

    override fun activatorTypes(): InstanceRegistry<ActivatorType> = activatorTypeInstances

    override fun restrictions(): InstanceRegistry<RestrictionType<*>> = restrictionTypeInstances

    override fun blockAttributes(): InstanceRegistry<BlockAttributeType<*>> = blockAttributeTypeInstances

    override fun activatorAttributes(): FactoryRegistry<ActivatorAttribute> = activatorAttributeFactories

    override fun activatorConditions(): FactoryRegistry<ActivatorCondition> = activatorConditionFactories

    override fun registerItemPlaceholders(provider: ItemPlaceholderProvider) {
        if (rejectIfClosed("item placeholder provider", provider::class.java.name)) return
        itemPlaceholderProviders += provider
    }

    override fun cooldowns(): FactoryRegistry<Cooldown> = cooldownFactories

    override fun cooldownFeedback(): FactoryRegistry<CooldownFeedback> = cooldownFeedbackFactories

    override fun itemAttributes(): FactoryRegistry<CustomItemStackAttribute> = itemAttributeFactories

    override fun itemIdentifiers(): InstanceRegistry<ItemIdentifier> = itemIdentifierInstances

    override fun isOpen(): Boolean = open

    fun close() {
        open = false
    }

    private var dispatchers: ListenerRegistry? = null
    private var boundListeners: ListenerRegistry? = null

    /**
     * Registers every dispatcher with Bukkit, once. Dispatchers outlive a reload — only the items
     * bound to them are rebuilt — so this is a no-op after the first call.
     */
    fun registerDispatchers(plugin: Plugin) {
        if (boundListeners == null) boundListeners = ListenerRegistry(plugin)
        if (dispatchers != null) return
        dispatchers = ListenerRegistry(plugin).apply {
            allDispatchers().forEach { register(it) }
        }
    }

    fun unregisterDispatchers() {
        boundListeners?.close()
        boundListeners = null
        dispatchers?.close()
        dispatchers = null
    }

    /**
     * Registers a listener that belongs to one item's binding rather than to a dispatcher — an
     * attribute holding per-item state. Unlike a dispatcher it is dropped on every reload.
     */
    fun registerBoundListener(listener: Listener) {
        val registry = boundListeners
        if (registry == null) {
            CustomItems.logger.warn("Listener {} registered before the plugin finished loading", listener::class.simpleName)
            return
        }
        registry.register(listener)
    }

    /** Drops every item's bindings, so a reload can rebuild them without re-registering listeners. */
    fun clearBindings() {
        activatorTypeInstances.all().forEach { it.clearBindings() }
        restrictionTypeInstances.all().forEach { it.clearBindings() }
        blockAttributeTypeInstances.all().forEach { it.clearBindings() }
        CooldownService.clear()
        boundListeners?.close()
    }

    private fun allDispatchers(): List<Listener> =
        activatorTypeInstances.all() + restrictionTypeInstances.all() + blockAttributeTypeInstances.all()

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

    fun all(): List<T> = instances.values.toList()

}
