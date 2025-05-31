package org.bigcraft.customitems.item

import me.wyne.wutils.config.configurables.ItemConfigurable
import me.wyne.wutils.config.configurables.attribute.GenericAttributeContainer
import me.wyne.wutils.config.configurables.item.ItemAttribute
import me.wyne.wutils.i18n.I18n
import me.wyne.wutils.i18n.language.component.PlaceholderLocalizedComponent
import net.kyori.adventure.audience.Audience
import org.bigcraft.customitems.ConfigurableFactory
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.attribute.*
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class CustomItem(
    private val key: String,
    private val itemAttributes: ItemConfigurable,
    private val restrictions: GenericAttributeContainer<Restriction<*>>
) {

    fun createItemStack(): ItemStack {
        val itemStack = itemAttributes.build()
        itemStack.editMeta { meta -> meta.persistentDataContainer.set(ITEM_TAG_KEY, PersistentDataType.STRING, key) }
        return itemStack
    }

    fun createItemStack(player: Player): ItemStack {
        val itemStack = itemAttributes.build(player)
        itemStack.editMeta { meta -> meta.persistentDataContainer.set(ITEM_TAG_KEY, PersistentDataType.STRING, key) }
        return itemStack
    }

    infix fun isCustomItem(item: ItemStack?): Boolean =
        item?.itemMeta?.persistentDataContainer?.get(ITEM_TAG_KEY, PersistentDataType.STRING) == key

    fun getName(localeContainer: Any): PlaceholderLocalizedComponent =
        I18n.global.getPlaceholderComponent(
            I18n.toLocale(localeContainer),
            I18n.toPlayer(localeContainer),
            itemAttributes.getValue(ItemAttribute.NAME, "")
        )

    object Factory : ConfigurableFactory<CustomItem> {
        override fun fromConfig(config: ConfigurationSection): CustomItem =
            CustomItem(
                config.name,
                ItemConfigurable().with(ITEM_ATTRIBUTE_MAP).also { it.fromConfig(config) },
                GenericAttributeContainer<Restriction<*>>(ITEM_RESTRICTION_MAP).also { it.fromConfig(config.getConfigurationSection(RESTRICTION_SECTION_KEY)) }
            ).apply {
                restrictions.attributes.values.forEach { it.apply(this) }
            }
    }

    companion object {
        const val RESTRICTION_SECTION_KEY = "restrictions"

        val ITEM_TAG_KEY = NamespacedKey(CustomItems.instance, "ci-key")
    }

}