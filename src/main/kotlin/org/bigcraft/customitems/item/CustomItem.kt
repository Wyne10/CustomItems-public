package org.bigcraft.customitems.item

import me.wyne.wutils.config.configurables.AttributeConfigurable
import me.wyne.wutils.config.configurables.ItemConfigurable
import me.wyne.wutils.config.configurables.attribute.AttributeContainerBuilder
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import me.wyne.wutils.config.configurables.item.ItemAttribute
import me.wyne.wutils.i18n.I18n
import me.wyne.wutils.i18n.language.component.PlaceholderLocalizedComponent
import org.bigcraft.customitems.ConfigurableFactory
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.activator.ACTIVATOR_MAP
import org.bigcraft.customitems.activator.Activator
import org.bigcraft.customitems.attribute.*
import org.bigcraft.customitems.block.CustomBlockManager
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.persistence.PersistentDataType

class CustomItem(
    val key: String,
    private val itemAttributes: ItemConfigurable,
    private val restrictions: Set<Restriction>,
    private val activators: Set<Activator>,
    private val blockAttributes: Set<BlockAttribute<*>>,
    val cooldowns: MutableSet<Cooldown>
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

    infix fun isCustomItem(block: Block): Boolean =
        isBlock() && CustomBlockManager.instance.getCustomBlock(block)?.itemKey == key

    infix fun hasCustomItem(inventory: Inventory): Boolean =
        inventory.filterNotNull().any { isCustomItem(it) }

    infix fun getItem(inventory: Inventory): ItemStack? =
        inventory.filterNotNull().firstOrNull { isCustomItem(it) }

    infix fun getItem(inventory: PlayerInventory): ItemStack? {
        val mainHandSlot = inventory.heldItemSlot
        val iterationOrder = mutableSetOf(mainHandSlot, 40, 36, 37, 38, 39)
        iterationOrder.addAll(INVENTORY_INDICES)
        return iterationOrder.mapNotNull { inventory.getItem(it) }
            .firstOrNull { isCustomItem(it) }
    }

    fun isBlock(): Boolean =
        itemAttributes.getValue(ItemAttribute.MATERIAL.key, Material.STONE).isBlock && blockAttributes.isNotEmpty()

    fun getName(localeContainer: Any): PlaceholderLocalizedComponent =
        I18n.global.accessor(localeContainer, itemAttributes.getValue(ItemAttribute.NAME.key))
            .getPlaceholderComponent(I18n.toPlayer(localeContainer))

    object Factory : ConfigurableFactory<CustomItem> {
        override fun fromConfig(config: ConfigurationSection): CustomItem =
            fromConfig(config.name, config)

        fun fromConfig(key: String, config: ConfigurationSection): CustomItem =
            CustomItem(
                key,
                ItemConfigurable(
                    AttributeContainerBuilder()
                        .with(ItemConfigurable.ITEM_ATTRIBUTE_MAP)
                        .with(ITEM_ATTRIBUTE_MAP)
                        .build(),
                    config
                ),
                AttributeConfigurable(
                    ImmutableAttributeContainer(RESTRICTION_MAP),
                    config.getConfigurationSection(RESTRICTION_SECTION_KEY)
                ).getSet(Restriction::class.java),
                AttributeConfigurable(
                    ImmutableAttributeContainer(ACTIVATOR_MAP),
                    config.getConfigurationSection(ACTIVATOR_SECTION_KEY)
                ).getSet(Activator::class.java),
                AttributeConfigurable(
                    ImmutableAttributeContainer(BLOCK_ATTRIBUTE_MAP),
                    config.getConfigurationSection(BLOCK_SECTION_KEY)
                ).getSet(BlockAttribute::class.java),
                AttributeConfigurable(
                    ImmutableAttributeContainer(COOLDOWN_MAP),
                    config.getConfigurationSection(COOLDOWN_SECTION_KEY)
                ).getSet(Cooldown::class.java)
            ).apply {
                restrictions.forEach { it.apply(this) }
                activators.forEach { it.apply(this) }
                blockAttributes.forEach { it.apply(this) }
            }
    }

    companion object {
        const val RESTRICTION_SECTION_KEY = "restrictions"
        const val ACTIVATOR_SECTION_KEY = "activators"
        const val BLOCK_SECTION_KEY = "block"
        const val COOLDOWN_SECTION_KEY = "cooldowns"

        val ITEM_TAG_KEY = NamespacedKey(CustomItems.instance, "ci-key")
        val INVENTORY_INDICES = (0..35).toSet()
    }

}

fun ItemStack?.isCustomItem(): Boolean =
    this?.itemMeta?.persistentDataContainer?.has(CustomItem.ITEM_TAG_KEY, PersistentDataType.STRING) ?: false