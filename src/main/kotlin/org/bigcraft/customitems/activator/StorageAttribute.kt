package org.bigcraft.customitems.activator

import com.google.gson.JsonPrimitive
import me.wyne.wutils.common.event.RegisterableListener
import me.wyne.wutils.common.serialization.Base64InventorySerializer
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.collections.remove

class StorageAttribute(
    key: String,
    columns: Int,
) : ActivatorAttribute<Int>(key, columns.coerceIn(0, 5)), RegisterableListener {

    private val players = mutableMapOf<Player, Inventory>()
    private val inventories = mutableMapOf<Inventory, Int>()

    init {
        CustomItemManager.instance.activatorRegistry.register(this)
    }

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        event is PlayerInteractEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as PlayerInteractEvent
        if (players.containsKey(player)) {
            player.closeInventory()
            return
        }
        val inventory = Bukkit.createInventory(null, InventoryType.SHULKER_BOX, item.itemMeta.displayName)
        players[player] = inventory
        if (player.inventory.getItem(EquipmentSlot.HAND) == item)
            inventories[inventory] = player.inventory.heldItemSlot
        else
            inventories[inventory] = 40
        readInventory(inventory, item)
        player.openInventory(inventory)
    }

    @EventHandler
    private fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!inventories.contains(event.inventory)) return
        val size = (value * 3) + ((value - 1).coerceAtLeast(0) * 3)
        for (i in SLOT_INDICES.size - 1 downTo size) {
            val slot = SLOT_INDICES[i]
            event.inventory.setItem(slot, FILLER)
        }
    }

    @EventHandler
    private fun onInventoryClose(event: InventoryCloseEvent) {
        if (!inventories.contains(event.inventory)) return
        val inventory = players.remove(event.player)
        val itemSlot = inventories.remove(event.inventory)!!
        val item = event.player.inventory.getItem(itemSlot) ?: return
        writeInventory(event.inventory, item)
    }

    @EventHandler
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        val inventory = players.remove(event.entity) ?: return
        val itemSlot = inventories.remove(inventory) ?: return
        val item = event.entity.inventory.getItem(itemSlot) ?: return
        writeInventory(inventory, item)
    }

    @EventHandler
    private fun onInventoryClick(event: InventoryClickEvent) {
        val inventory = players[event.whoClicked as Player] ?: return
        val slot = inventories[inventory]
        if (event.currentItem == FILLER) event.isCancelled = true
        if (event.clickedInventory == event.view.bottomInventory && slot == event.slot) {
            event.isCancelled = true
            return
        }
        if (event.click == ClickType.NUMBER_KEY) {
            val hotbarSlot = event.hotbarButton
            if (slot == hotbarSlot) {
                event.isCancelled = true
                return
            }
        }
        if (event.click == ClickType.SWAP_OFFHAND) {
            if (slot == 40)
                event.isCancelled = true
        }
/*        if (event.action == InventoryAction.DROP_ALL_SLOT ||
            event.action == InventoryAction.DROP_ONE_SLOT ||
            event.action == InventoryAction.DROP_ALL_CURSOR ||
            event.action == InventoryAction.DROP_ONE_CURSOR) {
            if (inventories.containsKey(event.view.topInventory))
                event.isCancelled = true
        }*/
    }

    @EventHandler
    private fun onCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        if (!players.containsKey(event.player)) return
        event.isCancelled = true
    }

    @EventHandler
    private fun onHandSwap(event: PlayerSwapHandItemsEvent) {
        if (!players.containsKey(event.player)) return
        event.isCancelled = true
    }

    @EventHandler
    private fun onItemDrop(event: PlayerDropItemEvent) {
        if (!players.containsKey(event.player)) return
        event.isCancelled = true
    }

    @EventHandler
    private fun onPlayerInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        if (!players.containsKey(event.player)) return
        event.isCancelled = true
    }

    @EventHandler
    private fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (!players.containsKey(event.player)) return
        event.isCancelled = true
    }

    private fun writeInventory(inventory: Inventory, item: ItemStack) {
        val serialized = SERIALIZER.serialize(inventory, null, null)
        val string = serialized.asString
        item.editMeta { meta ->
            meta.persistentDataContainer.set(STORAGE_KEY, PersistentDataType.STRING, string)
        }
    }

    private fun readInventory(inventory: Inventory, item: ItemStack) {
        val string = item.itemMeta.persistentDataContainer.get(STORAGE_KEY, PersistentDataType.STRING) ?: return
        val deserialized = SERIALIZER.deserialize(JsonPrimitive(string), null, null)
        inventory.setContents(deserialized.storageContents)
    }

    companion object {
        private val STORAGE_KEY = NamespacedKey(CustomItems.instance, "storage")
        private val SERIALIZER = Base64InventorySerializer()
        private val FILLER = ItemStack(Material.BLACK_STAINED_GLASS_PANE).apply {
            editMeta { meta -> meta.setDisplayName(" "); }
        }
        private val SLOT_INDICES = arrayOf(4, 13, 22, 5, 14, 23, 3, 12, 21, 6, 15, 24, 2, 11, 20, 7, 16, 25, 1, 10, 19, 8, 17, 26, 0, 9, 18)
    }

}