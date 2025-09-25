package org.bigcraft.customitems.item

import me.wyne.wutils.config.configurables.ItemConfigurable
import me.wyne.wutils.config.configurables.item.ItemAttribute
import me.wyne.wutils.config.configurables.item.attribute.LoreAttribute
import me.wyne.wutils.config.configurables.item.attribute.NameAttribute
import me.wyne.wutils.i18n.I18n
import me.wyne.wutils.i18n.language.component.PlaceholderLocalizedComponent
import org.bigcraft.customitems.activator.Activator
import org.bigcraft.customitems.attribute.BlockAttribute
import org.bigcraft.customitems.attribute.Restriction
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface ItemAttributeAccessor {
    val isBlock: Boolean
    val isCompositor: Boolean
    val composableSet: Set<String>
    fun getName(localeContainer: Any?): PlaceholderLocalizedComponent
    fun updateView(item: ItemStack)
}

class DefaultItemAttributeAccessor(
    private val itemAttributes: ItemConfigurable,
    private val restrictions: Set<Restriction>,
    private val activators: Set<Activator>,
    private val blockAttributes: Set<BlockAttribute<*>>,
) : ItemAttributeAccessor {
    override val isBlock: Boolean
        get() = itemAttributes.getValue(ItemAttribute.MATERIAL.key, Material.STONE).isBlock && blockAttributes.isNotEmpty()

    override val isCompositor: Boolean
        get() = itemAttributes.attributes.contains("compositor")

    override val composableSet: Set<String>
        get() = itemAttributes.getValue("compositor", emptySet())

    override fun getName(localeContainer: Any?): PlaceholderLocalizedComponent =
        I18n.global.accessor(localeContainer, itemAttributes.getValue(ItemAttribute.NAME.key))
            .getPlaceholderComponent(I18n.toPlayer(localeContainer))

    override fun updateView(item: ItemStack) {
        val customItem = CustomItemManager.instance.getItem(item) ?: return
        itemAttributes.get<NameAttribute>(ItemAttribute.NAME.key)?.apply(item)
        itemAttributes.get<LoreAttribute>(ItemAttribute.LORE.key)?.apply(item)
        item.editMeta {
            if (!it.hasLore()) return@editMeta
            var index = it.lore?.indexOfFirst { line -> line.contains("<composable-list>") } ?: return@editMeta
            val newLore = it.loreComponents!!.toMutableList()
            newLore.removeAt(index)
            for (composable in customItem.getComposableList(item, null)) {
                newLore.add(index, composable.bungee())
                index++
            }
            it.loreComponents = newLore
        }
    }
}