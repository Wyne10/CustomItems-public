package org.bigcraft.customitems.item

import org.bigcraft.customitems.ConfigurableFactory
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.attribute.*
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

const val RESTRICTION_SECTION_KEY = "restrictions"

val DEFAULT_MATERIAL = Material.STONE
val ITEM_TAG_KEY = NamespacedKey(CustomItems.instance, "ci-key")

class CustomItem(
    private val key: String,
    private val itemAttributes: Map<String, Attribute<ItemStack, *>>,
    private val customItemAttributes: Map<String, Attribute<CustomItem, *>>
) {

    fun createItemStack(): ItemStack {
        val itemStack = ItemStack(DEFAULT_MATERIAL)
        itemStack.editMeta { meta -> meta.persistentDataContainer.set(ITEM_TAG_KEY, PersistentDataType.STRING, key) }
        itemAttributes.forEach { it.value.apply(itemStack) }
        customItemAttributes.forEach { it.value.apply(this) }
        return itemStack
    }

    fun createItemStack(player: Player): ItemStack {
        val itemStack = ItemStack(DEFAULT_MATERIAL)
        itemStack.editMeta { meta -> meta.persistentDataContainer.set(ITEM_TAG_KEY, PersistentDataType.STRING, key) }
        itemAttributes
            .forEach {
                when (val attribute = it.value) {
                    is PlayerAwareAttribute -> attribute.apply(itemStack, player)
                    else -> attribute.apply(itemStack)
                }
            }
        customItemAttributes.forEach { it.value.apply(this) }
        return itemStack
    }

    fun isCustomItem(item: ItemStack?): Boolean =
        item?.itemMeta?.persistentDataContainer?.has(ITEM_TAG_KEY, PersistentDataType.STRING) ?: false

    object Factory : ConfigurableFactory<CustomItem> {
        override fun fromConfig(config: ConfigurationSection): CustomItem =
            CustomItem(
                config.name,
                ItemAttributes.createAllMap(config) + MetaAttributes.createAllMap(config),
                if (config.contains(RESTRICTION_SECTION_KEY))
                    Restrictions.createAllMap(config.getConfigurationSection(RESTRICTION_SECTION_KEY)!!)
                else
                    emptyMap()
            )
    }

}