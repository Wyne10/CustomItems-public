package me.wyne.customitems.item

import me.wyne.wutils.common.kotlin.config.deep
import me.wyne.wutils.config.configurables.AttributeConfigurable
import me.wyne.wutils.config.configurables.ItemConfigurable
import me.wyne.wutils.config.configurables.attribute.AttributeContainerBuilder
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.attribute.GenericFactory
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import me.wyne.wutils.config.configurables.item.ContextItemStackAttribute
import me.wyne.wutils.config.configurables.item.ItemAttribute
import me.wyne.wutils.config.configurables.item.ItemAttributeContext
import me.wyne.wutils.config.configurables.item.ItemStackAttribute
import me.wyne.wutils.config.configurables.item.attribute.LoreAttribute
import me.wyne.wutils.config.configurables.item.attribute.NameAttribute
import me.wyne.wutils.i18n.I18n
import me.wyne.wutils.i18n.language.component.PlaceholderLocalizedComponent
import net.kyori.adventure.text.Component
import me.wyne.customitems.CustomItems
import me.wyne.customitems.activator.bindActivators
import me.wyne.customitems.api.CustomItemApi
import me.wyne.customitems.block.CustomBlockManager
import me.wyne.customitems.block.bindBlockAttributes
import me.wyne.customitems.composition.ComposedKeys
import me.wyne.customitems.context.ItemContext
import me.wyne.customitems.context.expandLoreLines
import me.wyne.customitems.cooldown.CooldownGroup
import me.wyne.customitems.cooldown.createCooldowns
import me.wyne.customitems.i18n
import me.wyne.customitems.restriction.bindRestrictions
import me.wyne.customitems.spi.Registries
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
    private val key: String,
    private val itemAttributes: ItemConfigurable,
    private val hasBlockAttributes: Boolean,
    val cooldowns: CooldownGroup,
) : CustomItemApi {

    var path: String = ""

    /** Whether this item is placed as a tracked custom block rather than a plain one. */
    val isBlock: Boolean
        get() = material.isBlock && hasBlockAttributes

    /** The item keys this item accepts being composed with, empty if it is not a compositor. */
    val compositorSet: Set<String>
        get() = itemAttributes.getValue<Set<String>>(COMPOSITOR_ATTRIBUTE, emptySet()) ?: emptySet()

    override fun getKey(): String = key

    override fun getMaterial(): Material =
        itemAttributes.getValue(ItemAttribute.MATERIAL.key, Material.STONE) ?: Material.STONE

    /** This item's display name, falling back to the material's translation when it has none. */
    fun getName(localeContainer: Any?): PlaceholderLocalizedComponent {
        val name = itemAttributes.getValue<String>(ItemAttribute.NAME.key)
        if (name.isNullOrBlank()) return PlaceholderLocalizedComponent(
            i18n.component(),
            i18n.getLanguage(I18n.toLocale(localeContainer)),
            material.translationKey,
            Component.translatable(material.translationKey),
            i18n.audiences,
            I18n.toPlayer(localeContainer)
        )
        return i18n.accessor(localeContainer, name).getPlaceholderComponent(I18n.toPlayer(localeContainer))
    }

    override fun createItemStack(player: Player?): ItemStack {
        val stack = itemAttributes.build(ItemAttributeContext(player))
        tag(stack)
        refreshView(stack, player)
        return stack
    }

    override fun applyItemStack(item: ItemStack, player: Player?) {
        val context = ItemAttributeContext(player)
        itemAttributes.getSet(ItemStackAttribute::class.java).forEach { attribute ->
            if (attribute is ContextItemStackAttribute) attribute.apply(item, context) else attribute.apply(item)
        }
        tag(item)
        refreshView(item, player)
    }

    /** Re-renders [stack]'s name and lore against the placeholders its current state supplies. */
    fun refreshView(stack: ItemStack, player: Player?) =
        renderView(stack, ItemContext.of(this, stack, player))

    private fun renderView(stack: ItemStack, context: ItemContext) {
        itemAttributes.get<NameAttribute>(ItemAttribute.NAME.key)?.apply(stack, context.attributes)
        itemAttributes.get<LoreAttribute>(ItemAttribute.LORE.key)?.apply(stack, context.attributes)
        if (context.lineExpansions.isEmpty()) return
        val template = itemAttributes.getValue<List<String>>(ItemAttribute.LORE.key) ?: return
        expandLoreLines(stack, template, context.lineExpansions)
    }

    private fun tag(stack: ItemStack) =
        stack.editMeta { meta -> meta.persistentDataContainer.set(ITEM_TAG_KEY, PersistentDataType.STRING, key) }

    override infix fun isCustomItem(item: ItemStack?): Boolean {
        if (item == null || !item.hasItemMeta()) return false
        val pdc = item.itemMeta.persistentDataContainer
        if (pdc.get(ITEM_TAG_KEY, PersistentDataType.STRING) == key) return true
        return key in ComposedKeys.parse(pdc.get(COMPOSE_TAG_KEY, PersistentDataType.STRING))
    }

    override infix fun isCustomItem(block: Block): Boolean =
        isBlock && CustomBlockManager.instance.getCustomBlock(block)?.itemKey == key

    override infix fun getItem(inventory: Inventory): ItemStack? =
        inventory.filterNotNull().firstOrNull { isCustomItem(it) }

    override infix fun getItem(inventory: PlayerInventory): ItemStack? =
        (sequenceOf(inventory.heldItemSlot) + PRIORITY_SLOTS.asSequence() + INVENTORY_INDICES.asSequence())
            .distinct()
            .mapNotNull { inventory.getItem(it) }
            .firstOrNull { isCustomItem(it) }

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

        const val COMPOSITOR_ATTRIBUTE = "compositor"

        val ITEM_TAG_KEY = NamespacedKey(CustomItems.instance, "ci-key")
        val COMPOSE_TAG_KEY = NamespacedKey(CustomItems.instance, "ci-compose")

        /** Off-hand, then armor, checked before the main inventory. */
        private val PRIORITY_SLOTS = listOf(40, 36, 37, 38, 39)
        private val INVENTORY_INDICES = (0..35).toList()
    }

}
