package org.bigcraft.customitems.item

import me.wyne.wutils.config.configurables.ItemConfigurable
import me.wyne.wutils.config.configurables.item.ItemAttribute
import me.wyne.wutils.config.configurables.item.ItemAttributeContext
import me.wyne.wutils.config.configurables.item.attribute.LoreAttribute
import me.wyne.wutils.config.configurables.item.attribute.NameAttribute
import me.wyne.wutils.i18n.I18n
import org.bigcraft.customitems.i18n
import me.wyne.wutils.i18n.language.component.PlaceholderLocalizedComponent
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface ItemAttributeAccessor {
    val isBlock: Boolean
    val isCompositor: Boolean
    val compositorSet: Set<String>
    fun getName(localeContainer: Any?): PlaceholderLocalizedComponent
    fun updateView(item: ItemStack, context: ItemAttributeContext = ItemAttributeContext())
}

class DefaultItemAttributeAccessor(
    private val itemAttributes: ItemConfigurable,
    private val hasBlockAttributes: Boolean,
) : ItemAttributeAccessor {
    private val material: Material
        get() = itemAttributes.getValue(ItemAttribute.MATERIAL.key, Material.STONE) ?: Material.STONE

    override val isBlock: Boolean
        get() = material.isBlock && hasBlockAttributes

    override val isCompositor: Boolean
        get() = itemAttributes.attributes.contains("compositor")

    override val compositorSet: Set<String>
        get() = itemAttributes.getValue<Set<String>>("compositor", emptySet()) ?: emptySet()

    override fun getName(localeContainer: Any?): PlaceholderLocalizedComponent {
        val name = itemAttributes.getValue<String>(ItemAttribute.NAME.key)
        if (name.isNullOrBlank()) return PlaceholderLocalizedComponent(
            i18n.component(),
            i18n.getLanguage(I18n.toLocale(localeContainer)),
            material.translationKey,
            Component.translatable(material.translationKey),
            i18n.audiences,
            I18n.toPlayer(localeContainer)
        )
        return i18n.accessor(localeContainer, name)
            .getPlaceholderComponent(I18n.toPlayer(localeContainer))
    }

    override fun updateView(item: ItemStack, context: ItemAttributeContext) {
        val customItem = CustomItemManager.instance.getItem(item) ?: return
        itemAttributes.get<NameAttribute>(ItemAttribute.NAME.key)?.apply(item, context)
        itemAttributes.get<LoreAttribute>(ItemAttribute.LORE.key)?.apply(item, context)
        item.editMeta {
            if (!it.hasLore()) return@editMeta
            var index = it.lore?.indexOfFirst { line -> line.contains("<composed-list>") } ?: return@editMeta
            if (index == -1) return@editMeta
            val newLore = it.loreComponents!!.toMutableList()
            val composableList = customItem.getComposedList(item, null)
            if (composableList.isEmpty())
                newLore.removeAt(index)
            else
                index++
            for (composable in composableList) {
                newLore.add(index, composable.bungee())
                index++
            }
            it.loreComponents = newLore
            it.lore = it.lore!!
                .map { line -> line.replace("<composed-list>", "") }
        }
    }
}