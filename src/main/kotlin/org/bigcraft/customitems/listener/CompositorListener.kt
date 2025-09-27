package org.bigcraft.customitems.listener

import com.google.inject.Inject
import com.google.inject.Singleton
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.activator.test
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
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
        if (e.action != InventoryAction.NOTHING) return
        if (anvil.result.isNullOrAir()) return
        if (itemManager.getItem(anvil.result) == null) return
        if (e.cursor.isNotNullOrAir() && !e.isShiftClick) return
        val player = e.whoClicked as Player
        if (anvil.repairCost > player.level) return
        e.isCancelled = true
        if (e.isShiftClick) {
            if (player.inventory.firstEmpty() == -1) return
            player.inventory.addItem(anvil.result!!)
        } else {
            player.setItemOnCursor(anvil.result!!)
        }
        player.level -= anvil.repairCost
        anvil.clear()
        player.location.world.playSound(player.location, Sound.BLOCK_ANVIL_USE, 1.0F, 1.0F)
        if (player.gameMode in ANVIL_DAMAGE_IMMUNITY) return
        if (!ANVIL_DAMAGE_CHANCE.test()) return
        val block = anvil.location?.block ?: return
        if (block.type !in ANVIL_TYPES) return
        val currentDamageIndex = ANVIL_DAMAGE_QUEUE.indexOf(block.type)
        val nextDamageIndex = currentDamageIndex + 1
        block.type = ANVIL_DAMAGE_QUEUE[nextDamageIndex]
        if (block.type == Material.AIR)
            block.world.playSound(block.location, Sound.BLOCK_ANVIL_DESTROY, 1.0F, 1.0F)
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

    companion object {
        const val ANVIL_DAMAGE_CHANCE = 0.12
        val ANVIL_DAMAGE_IMMUNITY = setOf(GameMode.CREATIVE, GameMode.SPECTATOR)
        val ANVIL_TYPES = setOf(Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL)
        val ANVIL_DAMAGE_QUEUE = (ANVIL_TYPES + Material.AIR).toList()
    }

}

fun ItemStack?.isNullOrAir() = this == null || this.type == Material.AIR

fun ItemStack?.isNotNullOrAir() = this != null && this.type != Material.AIR