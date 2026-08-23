package org.bigcraft.customitems.spi

import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.item.ContextItemStackAttribute
import me.wyne.wutils.config.configurables.item.ItemAttributeContext
import org.bigcraft.customitems.api.spi.ConfigFactory
import org.bigcraft.customitems.api.spi.CustomItemStackAttribute
import org.bigcraft.customitems.api.spi.FactoryRegistry
import org.bukkit.inventory.ItemStack

/**
 * Item attributes are the one vocabulary WUtils owns: they live in an `AttributeContainer` next to
 * the built-in `material`/`name`/`lore` attributes, so each registration is adapted into a WUtils
 * attribute rather than being resolved by this plugin.
 */
class ItemAttributeRegistry : FactoryRegistry<CustomItemStackAttribute> {

    private val factories = LinkedHashMap<String, AttributeFactory<*>>()

    val attributeMap: Map<String, AttributeFactory<*>>
        get() = factories

    override fun register(key: String, factory: ConfigFactory<out CustomItemStackAttribute>) {
        if (Registries.rejectIfClosed("item attribute", key)) return
        registerNative(key, AttributeFactory { resolvedKey, config ->
            AdaptedItemStackAttribute(resolvedKey, factory.create(resolvedKey, config))
        })
    }

    /** Registers a WUtils attribute directly, for the built-ins that need the richer contract. */
    fun registerNative(key: String, factory: AttributeFactory<*>) {
        factories[key] = factory
    }

    override fun contains(key: String): Boolean = factories.containsKey(key)

    override fun keys(): Set<String> = factories.keys

}

private class AdaptedItemStackAttribute(
    key: String,
    attribute: CustomItemStackAttribute,
) : AttributeBase<CustomItemStackAttribute>(key, attribute), ContextItemStackAttribute {

    override fun apply(item: ItemStack) = value.apply(item, null)

    override fun apply(item: ItemStack, context: ItemAttributeContext) = value.apply(item, context.player)

}
