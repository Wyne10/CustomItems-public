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
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class StorageAttribute(
    key: String,
    columns: Int,
) : ActivatorAttribute<Int>(key, columns.coerceIn(0, 5)), RegisterableListener {

    private val inventories = mutableMapOf<Inventory, ItemStack>()

    init {
        CustomItemManager.instance.activatorRegistry.register(this)
    }

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val inventory = Bukkit.createInventory(player, 27, item.itemMeta.displayName)
        inventories[inventory] = item
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
        val item = inventories.remove(event.inventory)!!
        writeInventory(event.inventory, item)
    }

    @EventHandler
    private fun onInventoryClick(event: InventoryClickEvent) {
        if (event.currentItem == FILLER) event.isCancelled = true
        if (!inventories.containsValue(event.currentItem)) return
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