package org.bigcraft.customitems.attribute

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

interface Attribute<T, V> {
    val key: String
    val value: V
    fun apply(obj: T) = Unit
}

abstract class AttributeBase<T, V>(
    override val key: String,
    override val value: V,
) : Attribute<T, V>

class EmptyAttribute<T>(
    key: String,
) : AttributeBase<T, Unit>(key, Unit)

class CompositeAttribute<T>(
    key: String,
    config: ConfigurationSection,
    factory: AttributeFactory<T>
) : AttributeBase<T, Map<String, Attribute<T, *>>>(
    key,
    config.getConfigurationSection(key)!!.getKeys(false).mapNotNull { factory.create(it, config.getConfigurationSection(key)!!) }.associateBy { it.key }
) {
    override fun apply(obj: T) {
        value.values.forEach { it.apply(obj) }
    }
}

interface PlayerAwareAttribute<T, V> : Attribute<T, V> {
    fun apply(obj: T, player: Player)
}

fun interface AttributeFactory<T> {
    fun create(key: String, config: ConfigurationSection): Attribute<T, *>
}

interface CompositeAttributeFactory<T> : AttributeFactory<T> {
    override fun create(key: String, config: ConfigurationSection): Attribute<T, *> {
        return if (config.isConfigurationSection(key))
            fromSection(key, config.getConfigurationSection(key)!!)
        else
            fromString(key, config.getString(key)!!)
    }

    fun fromSection(key: String, section: ConfigurationSection): Attribute<T, *>
    fun fromString(key: String, string: String): Attribute<T, *>
}

class AttributeMap<T>(private val keyMap: MutableMap<String, AttributeFactory<T>>) {

    fun put(key: String, factory: AttributeFactory<T>) = keyMap.put(key, factory)

    fun remove(key: String) = keyMap.remove(key)

    fun create(key: String, config: ConfigurationSection): Attribute<T, *>? =
        keyMap[key]?.create(key, config)

    fun createAll(config: ConfigurationSection): Set<Attribute<*, *>> =
        keyMap.keys
            .filter { config.contains(it) }
            .map { keyMap[it]!!.create(it, config) }
            .toSet()

    fun createAllMap(config: ConfigurationSection): Map<String, Attribute<T, *>> =
        keyMap.keys
            .filter { config.contains(it) }
            .associateWith { keyMap[it]!!.create(it, config) }

}