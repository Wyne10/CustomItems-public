package org.bigcraft.customitems.composition

import com.google.inject.Inject
import com.google.inject.Singleton
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.inventory.ItemStack

/**
 * Merging one custom item into another on an anvil. The stack being merged *into* is the compositor;
 * it lists the item keys it accepts under its `compositor` attribute.
 */
@Singleton
class CompositionService @Inject constructor(private val itemManager: CustomItemManager) {

    /** The item keys [compositor] will accept, empty if it is not a compositor at all. */
    fun composable(compositor: ItemStack): Set<String> =
        itemManager.getItem(compositor)?.compositorSet.orEmpty()

    fun isComposable(item: CustomItem, compositor: ItemStack): Boolean =
        item.key in composable(compositor)

    fun isComposed(item: CustomItem, compositor: ItemStack): Boolean =
        item.key in ComposedKeys.of(compositor)

    fun compose(item: CustomItem, compositor: ItemStack): ItemStack {
        ComposedKeys.write(compositor, ComposedKeys.of(compositor) + item.key)
        itemManager.getItem(compositor)?.refreshView(compositor, null)
        return compositor
    }
}
