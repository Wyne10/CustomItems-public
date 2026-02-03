package org.bigcraft.customitems.item

import me.wyne.wutils.config.configurables.ItemConfigurable
import me.wyne.wutils.config.configurables.item.ItemAttribute
import me.wyne.wutils.config.configurables.item.attribute.LoreAttribute
import me.wyne.wutils.config.configurables.item.attribute.NameAttribute
import me.wyne.wutils.i18n.I18n
import me.wyne.wutils.i18n.language.component.PlaceholderLocalizedComponent
import net.kyori.adventure.text.Component
import org.bigcraft.customitems.activator.Activator
import org.bigcraft.customitems.block.BlockAttribute
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface ItemAttributeAccessor {
    val material: Material
    val isBlock: Boolean
    val isCompositor: Boolean
    val compositorSet: Set<String>
    fun getName(localeContainer: Any?): PlaceholderLocalizedComponent
    fun updateView(item: ItemStack)
}

class DefaultItemAttributeAccessor(
    private val itemAttributes: ItemConfigurable,
    private val restrictions: Set<Restriction>,
    private val activators: Set<Activator>,
    private val blockAttributes: Set<BlockAttribute<*>>,
) : ItemAttributeAccessor {
    override val material: Material
        get() = itemAttributes.getValue(ItemAttribute.MATERIAL.key, Material.STONE)

    override val isBlock: Boolean
        get() = material.isBlock && blockAttributes.isNotEmpty()

    override val isCompositor: Boolean
        get() = itemAttributes.attributes.contains("compositor")

    override val compositorSet: Set<String>
        get() = itemAttributes.getValue("compositor", emptySet())

    override fun getName(localeContainer: Any?): PlaceholderLocalizedComponent {
        val name = itemAttributes.getValue<String>(ItemAttribute.NAME.key)
        if (name.isNullOrBlank()) return PlaceholderLocalizedComponent(I18n.global.component(), I18n.global.getLanguage(
                I18n.toLocale(localeContainer)), material.translationKey, Component.translatable(material.translationKey), I18n.global.audiences,
            I18n.toPlayer(localeContainer))
        return I18n.global.accessor(localeContainer, itemAttributes.getValue(ItemAttribute.NAME.key))
            .getPlaceholderComponent(I18n.toPlayer(localeContainer))
    }

    override fun updateView(item: ItemStack) {
        val customItem = CustomItemManager.instance.getItem(item) ?: return
        itemAttributes.get<NameAttribute>(ItemAttribute.NAME.key)?.apply(item)
        itemAttributes.get<LoreAttribute>(ItemAttribute.LORE.key)?.apply(item)
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