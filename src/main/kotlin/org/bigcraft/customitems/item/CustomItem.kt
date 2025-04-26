package org.bigcraft.customitems.item

import org.bigcraft.customitems.ConfigurableFactory
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.attribute.Attribute
import org.bigcraft.customitems.attribute.ItemAttributes
import org.bigcraft.customitems.attribute.MetaAttributes
import org.bigcraft.customitems.attribute.PlayerAwareAttribute
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

val DEFAULT_MATERIAL = Material.STONE
val ITEM_TAG_KEY = NamespacedKey(CustomItems.instance, "ci-key")

class CustomItem(
    private val key: String,
    private val attributes: Map<String, Attribute<*>>
) {

    fun createItemStack(): ItemStack {
        val itemStack = ItemStack(DEFAULT_MATERIAL)
        itemStack.itemMeta.persistentDataContainer.set(ITEM_TAG_KEY, PersistentDataType.STRING, key)
        attributes.forEach { it.value.apply(itemStack) }
        return itemStack
    }

    fun createItemStack(player: Player): ItemStack {
        val itemStack = ItemStack(DEFAULT_MATERIAL)
        itemStack.itemMeta.persistentDataContainer.set(ITEM_TAG_KEY, PersistentDataType.STRING, key)
        attributes
            .forEach {
                when (val attribute = it.value) {
                    is PlayerAwareAttribute -> attribute.apply(itemStack, player)
                    else -> attribute.apply(itemStack)
                }
            }
        return itemStack
    }

    object Factory : ConfigurableFactory<CustomItem> {
        override fun fromConfig(config: ConfigurationSection): CustomItem =
            CustomItem(
                config.name,
                ItemAttributes.createAllMap(config) +
                        MetaAttributes.createAllMap(config)
            )
    }

}