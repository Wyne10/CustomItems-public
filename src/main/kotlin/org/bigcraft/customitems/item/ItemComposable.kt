package org.bigcraft.customitems.item

import me.wyne.wutils.i18n.I18n
import org.bigcraft.customitems.i18n
import me.wyne.wutils.i18n.kotlin.replace
import me.wyne.wutils.i18n.language.component.PlaceholderLocalizedComponent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

interface ItemComposable {
    fun isComposable(compositor: ItemStack): Boolean
    fun isComposed(compositor: ItemStack): Boolean
    fun compose(compositor: ItemStack): ItemStack
    fun getComposable(compositor: ItemStack): Set<String>
    fun getComposed(compositor: ItemStack): Set<String>
    fun getComposedList(compositor: ItemStack, localeContainer: Any?): List<PlaceholderLocalizedComponent>
}

class DefaultItemComposable(
    private val key: String
) : ItemComposable {
    override fun isComposable(compositor: ItemStack): Boolean {
        val composable = getComposable(compositor)
        return composable.contains(key)
    }

    override fun isComposed(compositor: ItemStack): Boolean {
        if (!compositor.hasItemMeta()) return false
        val composed = compositor.itemMeta.persistentDataContainer
            .get(CustomItem.COMPOSE_TAG_KEY, PersistentDataType.STRING) ?: return false
        return composed == key
                || composed.startsWith("$key,")
                || composed.endsWith(",$key")
                || composed.contains(",$key,")
    }

    override fun compose(compositor: ItemStack): ItemStack {
        val customItem = CustomItemManager.instance.getItem(compositor)
        compositor.editMeta { meta ->
            var composed = compositor.itemMeta?.persistentDataContainer?.get(CustomItem.COMPOSE_TAG_KEY, PersistentDataType.STRING) ?: ""
            if (composed.isEmpty())
                composed = key
            else
                composed += ",${key}"
            meta.persistentDataContainer.set(CustomItem.COMPOSE_TAG_KEY, PersistentDataType.STRING, composed)
        }
        customItem?.updateView(compositor)
        return compositor
    }

    override fun getComposable(compositor: ItemStack): Set<String> {
        val customItem = CustomItemManager.instance.getItem(compositor) ?: return emptySet()
        if (!customItem.isCompositor) return emptySet()
        return customItem.compositorSet
    }

    override fun getComposed(compositor: ItemStack): Set<String> {
        if (!compositor.hasItemMeta()) return emptySet()
        val composedString = compositor.itemMeta.persistentDataContainer
            .get(CustomItem.COMPOSE_TAG_KEY, PersistentDataType.STRING) ?: return emptySet()
        return composedString.split(",").toSet()
    }

    override fun getComposedList(compositor: ItemStack, localeContainer: Any?): List<PlaceholderLocalizedComponent> =
        getComposed(compositor)
            .map { i18n.accessor(localeContainer, "format-composable-entry")
                    .getPlaceholderComponent(I18n.toPlayer(localeContainer), "key" replace it) }
}