package org.bigcraft.customitems.listener

import com.google.inject.Inject
import com.google.inject.Singleton
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareAnvilEvent
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
        e.result = composeItems(anvil.firstItem!!.clone(), anvil.secondItem!!)
    }

    @EventHandler
    private fun onInventoryClick(e: InventoryClickEvent) {
        if (e.clickedInventory?.type != InventoryType.ANVIL) return
        if (e.slot != 2) return
        if (e.cursor.isNotNullOrAir() && !e.isShiftClick) return
        if (itemManager.getItem(e.currentItem) == null) return
        val player = e.whoClicked as? Player ?: return
        e.isCancelled = true
        player.location.world.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1.0F, 1.0F)
        if (e.isShiftClick) {
            if (player.inventory.firstEmpty() == -1) return
            player.inventory.addItem(e.currentItem!!)
        } else {
            player.setItemOnCursor(e.currentItem)
        }
        e.clickedInventory!!.clear()
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