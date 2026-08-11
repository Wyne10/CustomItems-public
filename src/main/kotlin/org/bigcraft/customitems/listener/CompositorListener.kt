package org.bigcraft.customitems.listener

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.common.anvil.AnvilUtils
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
    private val itemManager: CustomItemManager
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
        Bukkit.getScheduler().runTaskLater(CustomItems.instance, Runnable {
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

    private fun composeItems(compositor: ItemStack, composable: ItemStack): ItemStack {
        val composable = itemManager.getItem(composable)!!
        return composable.compose(compositor)
    }

    private fun isInputValid(compositor: ItemStack, composable: ItemStack): Boolean {
        val composable = itemManager.getItem(composable) ?: return false
        if (!composable.isComposable(compositor)) return false
        if (composable.isComposed(compositor)) return false
        return true
    }

}

fun ItemStack?.isNullOrAir() = this == null || this.type == Material.AIR

fun ItemStack?.isNotNullOrAir() = this != null && this.type != Material.AIR