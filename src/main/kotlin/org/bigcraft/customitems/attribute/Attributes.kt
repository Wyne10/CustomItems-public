package org.bigcraft.customitems.attribute

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface Attribute<V> {
    val key: String
    val value: V
    fun apply(item: ItemStack): ItemStack
}

abstract class AttributeBase<V>(
    override val key: String,
) : Attribute<V>

interface PlayerAwareAttribute<V> : Attribute<V> {
    fun apply(item: ItemStack, player: Player): ItemStack
}

fun interface AttributeFactory {
    fun create(key: String, config: ConfigurationSection): Attribute<*>
}

class AttributeMap(private val keyMap: MutableMap<String, AttributeFactory>) {

    fun put(key: String, factory: AttributeFactory) = keyMap.put(key, factory)

    fun remove(key: String) = keyMap.remove(key)

    fun create(key: String, config: ConfigurationSection): Attribute<*>? =
        keyMap[key]?.create(key, config)

    fun createAll(config: ConfigurationSection): Set<Attribute<*>> =
        keyMap.keys
            .filter { config.contains(it) }
            .map { keyMap[it]!!.create(it, config) }
            .toSet()

    fun createAllMap(config: ConfigurationSection): Map<String, Attribute<*>> =
        keyMap.keys
            .filter { config.contains(it) }
            .associateWith { keyMap[it]!!.create(it, config) }

}