package org.bigcraft.customitems.composition

import me.wyne.wutils.common.scheduler.Schedulers
import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.common.anvil.AnvilUtils
import me.wyne.wutils.common.kotlin.item.isNotNullOrAir
import me.wyne.wutils.common.kotlin.item.isNullOrAir
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack

@Singleton
class CompositorListener @Inject constructor(
    plugin: CustomItems,
    private val itemManager: CustomItemManager,
    private val composition: CompositionService,
) : Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    private fun onTryMerge(e: PrepareAnvilEvent) {
        val anvil = e.inventory
        if (anvil.firstItem.isNullOrAir() || anvil.secondItem.isNullOrAir()) return
        if (e.result.isNotNullOrAir()) return
        if (!isInputValid(anvil.firstItem!!, anvil.secondItem!!)) return
        Schedulers.sync().runLater({
            anvil.result = composeItems(anvil.firstItem!!.clone(), anvil.secondItem!!)
        }, 1)
    }

    @EventHandler(ignoreCancelled = true)
    private fun onInventoryClick(e: InventoryClickEvent) {
        val anvil = e.inventory as? AnvilInventory ?: return
        if (e.slotType != InventoryType.SlotType.RESULT) return
        if (anvil.result.isNullOrAir()) return
        if (itemManager.getItem(anvil.result) == null) return
        AnvilUtils.getResult(e)
    }

    private fun composeItems(compositor: ItemStack, composable: ItemStack): ItemStack =
        composition.compose(itemManager.getItem(composable)!!, compositor)

    private fun isInputValid(compositor: ItemStack, composable: ItemStack): Boolean {
        val item = itemManager.getItem(composable) ?: return false
        return composition.isComposable(item, compositor) && !composition.isComposed(item, compositor)
    }

}