package org.bigcraft.customitems.attribute

import me.wyne.wutils.common.event.RegisterableListener
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import org.bigcraft.customitems.extension.getEnumSet
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemAttribute
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.AnvilInventory

val ITEM_RESTRICTION_MAP = AttributeMap(
    mutableMapOf(
        "cancel-drop" to AttributeFactory { key, config -> DropRestriction(key, config.getBoolean(key)) },
        "cancel-place" to AttributeFactory { key, config -> PlaceRestriction(key, config.getBoolean(key)) },
        "cancel-craft" to AttributeFactory { key, config -> CraftRestriction(key, config.getBoolean(key)) },
        "cancel-craft-vanilla" to AttributeFactory { key, config -> VanillaCraftRestriction(key, config.getBoolean(key)) },
        "cancel-storage" to AttributeFactory { key, config -> StorageRestriction(key, config.getEnumSet<InventoryType>(key)) },
        "cancel-damage" to AttributeFactory { key, config -> DamageRestriction(key, config.getEnumSet<DamageCause>(key)) },
        "cancel-despawn" to AttributeFactory { key, config -> DespawnRestriction(key, config.getBoolean(key)) },
        "cancel-enchant" to AttributeFactory { key, config -> EnchantRestriction(key, config.getBoolean(key)) },
        "cancel-enchant-prepare" to AttributeFactory { key, config -> EnchantPrepareRestriction(key, config.getBoolean(key)) },
        "cancel-consumption" to AttributeFactory { key, config -> ConsumptionRestriction(key, config.getBoolean(key)) },
        "cancel-rename" to AttributeFactory { key, config -> RenameRestriction(key, config.getBoolean(key)) },
        "cancel-anvil" to AttributeFactory { key, config -> AnvilRestriction(key, config.getBoolean(key)) },
    )
)

abstract class Restriction(
    key: String,
    isActive: Boolean,
) : AttributeBase<Boolean>(key, isActive), CustomItemAttribute, RegisterableListener {

    protected lateinit var item: CustomItem
        private set

    final override fun apply(item: CustomItem) {
        this.item = item
        CustomItemManager.instance.restrictionRegistry.register(this) // TODO Check how often this is called
    }

}

class DropRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true)
    private fun onEvent(event: PlayerDropItemEvent) {
        if (!value) return
        if (item isCustomItem event.itemDrop.itemStack)
            event.isCancelled = true
    }
}

class PlaceRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true)
    private fun onEvent(event: BlockPlaceEvent) {
        if (!value) return
        if (item isCustomItem event.itemInHand && event.itemInHand.type.isBlock)
            event.isCancelled = true
    }
}

class CraftRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true)
    private fun onEvent(event: CraftItemEvent) {
        if (!value) return
        if (event.inventory.any(item::isCustomItem))
            event.isCancelled = true
    }
}

class VanillaCraftRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true)
    private fun onEvent(event: CraftItemEvent) {
        if (!value) return
        if (event.recipe is Keyed && (event.recipe as Keyed).key.namespace() == NamespacedKey.MINECRAFT) {
            if (event.inventory.any(item::isCustomItem))
                event.isCancelled = true
        }
    }
}

class StorageRestriction(
    key: String,
    private val types: Set<InventoryType>
) : Restriction(key, true) {
    @EventHandler(ignoreCancelled = true)
    private fun onEvent(event: InventoryClickEvent) {
        if (item isCustomItem event.currentItem && types.contains(event.view.topInventory.type))
            event.isCancelled = true
    }
}

class DamageRestriction(
    key: String,
    private val causes: Set<DamageCause>
) : Restriction(key, true) {
    @EventHandler(ignoreCancelled = true)
    private fun onEvent(event: EntityDamageEvent) {
        if (event.entityType == EntityType.DROPPED_ITEM && item isCustomItem (event.entity as Item).itemStack && causes.contains(event.cause))
            event.isCancelled = true
    }
}

class DespawnRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true)
    private fun onEvent(event: ItemDespawnEvent) {
        if (!value) return
        if (item isCustomItem event.entity.itemStack)
            event.isCancelled = true
    }
}

class EnchantRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true)
    private fun onEvent(event: EnchantItemEvent) {
        if (!value) return
        if (item isCustomItem event.item)
            event.isCancelled = true
    }
}

class EnchantPrepareRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true)
    private fun onEvent(event: PrepareItemEnchantEvent) {
        if (!value) return
        if (item isCustomItem event.item)
            event.isCancelled = true
    }
}

class ConsumptionRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true)
    private fun onEvent(event: PlayerItemConsumeEvent) {
        if (!value) return
        if (item isCustomItem event.item)
            event.isCancelled = true
    }
}

class RenameRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true)
    private fun onEvent(event: InventoryClickEvent) {
        if (!value) return
        if (event.clickedInventory !is AnvilInventory) return
        val inventory = event.clickedInventory as AnvilInventory
        if (!item.isCustomItem(inventory.firstItem)) return
        if (event.slot == 2 && (inventory.renameText != null || inventory.renameText?.isNotEmpty() == true))
            event.isCancelled = true
    }
}

class AnvilRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    private fun onEvent(event: InventoryClickEvent) {
        if (!value) return
        if (event.clickedInventory !is AnvilInventory) return
        val inventory = event.clickedInventory as AnvilInventory
        if (event.slot == 2 && (item isCustomItem inventory.firstItem || item isCustomItem inventory.secondItem))
            event.isCancelled = true
    }
}