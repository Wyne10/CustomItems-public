package org.bigcraft.customitems.item

import me.wyne.wutils.config.configurables.AttributeConfigurable
import me.wyne.wutils.config.configurables.ItemConfigurable
import me.wyne.wutils.config.configurables.attribute.AttributeContainerBuilder
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import me.wyne.wutils.config.configurables.item.ContextItemStackAttribute
import me.wyne.wutils.config.configurables.item.ItemAttributeContext
import me.wyne.wutils.config.configurables.item.ItemStackAttribute
import me.wyne.wutils.i18n.kotlin.replace
import me.wyne.wutils.i18n.language.replacement.ComponentReplacement
import org.bigcraft.customitems.ConfigurableFactory
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.activator.ACTIVATOR_MAP
import org.bigcraft.customitems.activator.Activator
import org.bigcraft.customitems.api.CustomItemApi
import org.bigcraft.customitems.block.BLOCK_ATTRIBUTE_MAP
import org.bigcraft.customitems.block.BlockAttribute
import org.bigcraft.customitems.block.CustomBlockManager
import org.bigcraft.customitems.cooldown.COOLDOWN_MAP
import org.bigcraft.customitems.cooldown.Cooldown
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
    private val restrictions: Set<Restriction>,
    private val activators: Set<Activator>,
    private val blockAttributes: Set<BlockAttribute<*>>,
    val cooldowns: MutableSet<Cooldown>,
) : ItemAttributeAccessor by DefaultItemAttributeAccessor(itemAttributes, restrictions, activators, blockAttributes),
    ItemComposable by DefaultItemComposable(key), CustomItemApi {

    var path: String = ""

    override fun getKey(): String = key

    override fun createItemStack(player: Player?): ItemStack {
        val itemStack = itemAttributes.build(player)
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

    object Factory : ConfigurableFactory<CustomItem> {
        override fun fromConfig(config: ConfigurationSection): CustomItem =
            fromConfig(config.name, config)

        fun fromConfig(key: String, config: ConfigurationSection): CustomItem =
            runCatching {
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
                    ).getSet(Cooldown::class.java),
                ).apply {
                    restrictions.forEach { it.apply(this) }
                    activators.forEach { it.apply(this) }
                    blockAttributes.forEach { it.apply(this) }
                }
            }.getOrElse {
                CustomItems.logger.error("Failed to load custom item {}", key, it)
                return CustomItem(key, ItemConfigurable(), emptySet(), emptySet(), emptySet(), mutableSetOf())
            }
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