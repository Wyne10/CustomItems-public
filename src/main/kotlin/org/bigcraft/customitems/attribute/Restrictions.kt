package org.bigcraft.customitems.attribute

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import org.bigcraft.customitems.config.getEnumSet
import org.bigcraft.customitems.item.CustomItemManager
import org.bigcraft.customitems.item.RegisterableCustomItemAttribute
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
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
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack

val RESTRICTION_MAP = AttributeMap(
    mapOf(
        "cancel-drop" to AttributeFactory { key, config -> DropRestriction(key, config.getBoolean(key)) },
        "cancel-place" to AttributeFactory { key, config -> PlaceRestriction(key, config.getBoolean(key)) },
        "cancel-craft" to AttributeFactory { key, config -> CraftRestriction(key, config.getBoolean(key)) },
        "cancel-craft-vanilla" to AttributeFactory { key, config -> VanillaCraftRestriction(key, config.getBoolean(key)) },
        "cancel-storage" to AttributeFactory { key, config -> StorageRestriction(key, config.getEnumSet<InventoryType>(key)) },
        "cancel-damage" to AttributeFactory { key, config -> DamageRestriction(key, config.getEnumSet<DamageCause>(key)) },
        "cancel-despawn" to AttributeFactory { key, config -> DespawnRestriction(key, config.getBoolean(key)) },
        "cancel-enchant" to AttributeFactory { key, config -> EnchantRestriction(key, config.getBoolean(key)) },
        "cancel-enchant-prepare" to AttributeFactory { key, config -> EnchantPrepareRestriction(key, config.getBoolean(key)) },
        "cancel-consume" to AttributeFactory { key, config -> ConsumptionRestriction(key, config.getBoolean(key)) },
        "cancel-rename" to AttributeFactory { key, config -> RenameRestriction(key, config.getBoolean(key)) },
        "cancel-anvil" to AttributeFactory { key, config -> AnvilRestriction(key, config.getBoolean(key)) },
        "cancel-projectile" to AttributeFactory { key, config -> ProjectileRestriction(key, config.getBoolean(key)) },
        "cancel-action" to AttributeFactory { key, config -> ActionRestriction(key, config.getEnumSet<Action>(key)) },
    )
)

abstract class Restriction(
    key: String,
    isActive: Boolean,
) : RegisterableCustomItemAttribute<Boolean>(key, isActive, CustomItemManager.instance.restrictionRegistry) {

    protected fun cancelEvent(event: Cancellable, itemStack: ItemStack?) {
        if (item isCustomItem itemStack)
            event.isCancelled = true
    }

}

class DropRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: PlayerDropItemEvent) {
        cancelEvent(event, event.itemDrop.itemStack)
    }
}

class PlaceRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: BlockPlaceEvent) {
        if (!event.itemInHand.type.isBlock) return
        cancelEvent(event, event.itemInHand)
    }
}

class CraftRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: CraftItemEvent) {
        if (event.inventory.any(item::isCustomItem))
            event.isCancelled = true
    }
}

class VanillaCraftRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: CraftItemEvent) {
        if (event.recipe is Keyed && (event.recipe as Keyed).key.namespace() == NamespacedKey.MINECRAFT) {
            if (event.inventory.any(item::isCustomItem))
                event.isCancelled = true
        }
    }
}

class StorageRestriction(
    key: String,
    private val types: Set<InventoryType>
) : Restriction(key, types.isNotEmpty()) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: InventoryClickEvent) {
        if (!types.contains(event.view.topInventory.type)) return
        cancelEvent(event, event.currentItem)
    }
}

class DamageRestriction(
    key: String,
    private val causes: Set<DamageCause>
) : Restriction(key, causes.isNotEmpty()) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: EntityDamageEvent) {
        if (event.entityType != EntityType.DROPPED_ITEM) return
        if (!causes.contains(event.cause)) return
        cancelEvent(event, (event.entity as Item).itemStack)
    }
}

class DespawnRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: ItemDespawnEvent) {
        cancelEvent(event, event.entity.itemStack)
    }
}

class EnchantRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: EnchantItemEvent) {
        cancelEvent(event, event.item)
    }
}

class EnchantPrepareRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: PrepareItemEnchantEvent) {
        cancelEvent(event, event.item)
    }
}

class ConsumptionRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: PlayerItemConsumeEvent) {
        cancelEvent(event, event.item)
    }
}

class RenameRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: InventoryClickEvent) {
        val inventory = event.clickedInventory as? AnvilInventory ?: return
        if (!item.isCustomItem(inventory.firstItem)) return
        if (event.slot == 2 && (inventory.renameText != null || inventory.renameText?.isNotEmpty() == true))
            event.isCancelled = true
    }
}

class AnvilRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: InventoryClickEvent) {
        val inventory = event.clickedInventory as? AnvilInventory ?: return
        if (event.slot == 2 && (item isCustomItem inventory.firstItem || item isCustomItem inventory.secondItem))
            event.isCancelled = true
    }
}

class ProjectileRestriction(key: String, isActive: Boolean) : Restriction(key, isActive) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: PlayerLaunchProjectileEvent) {
        cancelEvent(event, event.itemStack)
    }
}

class ActionRestriction(
    key: String,
    private val actions: Set<Action>
) : Restriction(key, actions.isNotEmpty()) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private fun onEvent(event: PlayerInteractEvent) {
        if (!actions.contains(event.action)) return
        cancelEvent(event, event.item)
    }
}