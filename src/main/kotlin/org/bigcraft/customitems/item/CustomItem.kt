package org.bigcraft.customitems.item

import me.wyne.wutils.config.configurables.AttributeConfigurable
import me.wyne.wutils.config.configurables.ItemConfigurable
import me.wyne.wutils.common.kotlin.config.deep
import me.wyne.wutils.config.configurables.attribute.AttributeContainerBuilder
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.attribute.GenericFactory
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import me.wyne.wutils.config.configurables.item.ContextItemStackAttribute
import me.wyne.wutils.config.configurables.item.ItemAttributeContext
import me.wyne.wutils.config.configurables.item.ItemStackAttribute
import me.wyne.wutils.i18n.kotlin.replace
import me.wyne.wutils.i18n.language.replacement.ComponentReplacement
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.activator.bindActivators
import org.bigcraft.customitems.restriction.bindRestrictions
import org.bigcraft.customitems.api.CustomItemApi
import org.bigcraft.customitems.block.bindBlockAttributes
import org.bigcraft.customitems.block.CustomBlockManager
import org.bigcraft.customitems.cooldown.CooldownGroup
import org.bigcraft.customitems.cooldown.createCooldowns
import org.bigcraft.customitems.spi.Registries
import me.wyne.wutils.config.configurables.item.ItemAttribute
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType

class CustomItem(
    private val key: String,
    private val itemAttributes: ItemConfigurable,
    private val hasBlockAttributes: Boolean,
    val cooldowns: CooldownGroup,
) : ItemAttributeAccessor by DefaultItemAttributeAccessor(itemAttributes, hasBlockAttributes),
    ItemComposable by DefaultItemComposable(key), CustomItemApi {

    var path: String = ""

    override fun getKey(): String = key

    override fun getMaterial(): Material =
        itemAttributes.getValue(ItemAttribute.MATERIAL.key, Material.STONE) ?: Material.STONE

    override fun createItemStack(player: Player?): ItemStack {
        val itemStack = itemAttributes.build(ItemAttributeContext(player))
        itemStack.editMeta { meta -> meta.persistentDataContainer.set(ITEM_TAG_KEY, PersistentDataType.STRING, key) }
        updateView(
            itemStack,
            ItemAttributeContext(
                player,
                arrayOf("durability" replace itemStack._durability),
                emptyArray<ComponentReplacement>()
            )
        ) // TODO Might add context replacements as attributes
        return itemStack
    }

    override fun applyItemStack(item: ItemStack, player: Player?) {
        val context = ItemAttributeContext(player)
        itemAttributes.getSet(ItemStackAttribute::class.java).forEach { attribute ->
            if (attribute is ContextItemStackAttribute) {
                attribute.apply(item, context)
            } else {
                attribute.apply(item)
            }
        }
        item.editMeta { meta -> meta.persistentDataContainer.set(ITEM_TAG_KEY, PersistentDataType.STRING, key) }
        updateView(
            item,
            ItemAttributeContext(
                player,
                arrayOf("durability" replace item._durability),
                emptyArray<ComponentReplacement>()
            )
        ) // TODO Might add context replacements as attributes
    }

    override infix fun isCustomItem(item: ItemStack?): Boolean {
        if (item == null || !item.hasItemMeta()) return false
        val pdc = item.itemMeta.persistentDataContainer
        if (pdc.get(ITEM_TAG_KEY, PersistentDataType.STRING) == key) return true
        val composed = pdc.get(COMPOSE_TAG_KEY, PersistentDataType.STRING) ?: return false
        return composed == key
                || composed.startsWith("$key,")
                || composed.endsWith(",$key")
                || composed.contains(",$key,")
    }

    override infix fun isCustomItem(block: Block): Boolean =
        isBlock && CustomBlockManager.instance.getCustomBlock(block)?.itemKey == key

    override infix fun getItem(inventory: Inventory): ItemStack? =
        inventory.filterNotNull().firstOrNull { isCustomItem(it) }

    override infix fun getItem(inventory: PlayerInventory): ItemStack? {
        val mainHandSlot = inventory.heldItemSlot
        val iterationOrder = mutableSetOf(mainHandSlot, 40, 36, 37, 38, 39)
        iterationOrder.addAll(INVENTORY_INDICES)
        return iterationOrder.mapNotNull { inventory.getItem(it) }
            .firstOrNull { isCustomItem(it) }
    }

    override infix fun getItems(inventory: Inventory): List<ItemStack> =
        inventory.filterNotNull().filter { isCustomItem(it) }

    object Factory : GenericFactory<CustomItem> {
        override fun create(key: String, config: ConfigurationSection): CustomItem =
            fromConfig(key, config.deep(key))

        fun fromConfig(key: String, config: ConfigurationSection): CustomItem =
            CustomItem(
                key,
                ItemConfigurable(
                    AttributeContainerBuilder()
                        .with(ItemConfigurable.ITEM_ATTRIBUTE_MAP)
                        .with(Registries.itemAttributeFactories.attributeMap)
                        .build(),
                    config
                ),
                config.section(BLOCK_SECTION_KEY).getKeys(false).isNotEmpty(),
                createCooldowns(key, config.section(COOLDOWN_SECTION_KEY)),
            ).apply {
                bindRestrictions(this, config.section(RESTRICTION_SECTION_KEY))
                bindBlockAttributes(this, config.section(BLOCK_SECTION_KEY))
                bindActivators(this, config.section(ACTIVATOR_SECTION_KEY))
            }

        private fun <T> ConfigurationSection.attributes(sectionKey: String, map: AttributeMap, type: Class<T>): Set<T> =
            AttributeConfigurable(ImmutableAttributeContainer(map), section(sectionKey)).getSet(type)

        private fun ConfigurationSection.section(sectionKey: String): ConfigurationSection =
            getConfigurationSection(sectionKey) ?: CustomItems.EMPTY_CONFIGURATION
    }

    companion object {
        const val RESTRICTION_SECTION_KEY = "restrictions"
        const val ACTIVATOR_SECTION_KEY = "activators"
        const val BLOCK_SECTION_KEY = "block"
        const val COOLDOWN_SECTION_KEY = "cooldowns"

        val ITEM_TAG_KEY = NamespacedKey(CustomItems.instance, "ci-key")
        val COMPOSE_TAG_KEY = NamespacedKey(CustomItems.instance, "ci-compose")
        val INVENTORY_INDICES = (0..35).toSet()
    }

}

val ItemStack._durability: Int
    get() = type.maxDurability - ((this.itemMeta as? Damageable)?.damage ?: 0)