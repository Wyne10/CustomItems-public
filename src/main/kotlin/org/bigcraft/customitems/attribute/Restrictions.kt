package org.bigcraft.customitems.attribute

import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.extension.getEnumSet
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent

val Restrictions = AttributeMap(
    mutableMapOf(
        "cancel-drop" to AttributeFactory { _, _ -> DropRestriction },
        "cancel-place" to AttributeFactory { _, _ -> PlaceRestriction },
        "cancel-craft" to AttributeFactory { _, _ -> CraftRestriction() },
        "cancel-craft-vanilla" to AttributeFactory { _, _ -> VanillaCraftRestriction },
        "cancel-storage" to AttributeFactory { key, config -> StorageRestriction(config.getEnumSet<InventoryType>(key)) },
        "cancel-damage" to AttributeFactory { key, config -> DamageRestriction(config.getEnumSet<DamageCause>(key)) },
    )
)

abstract class Restriction<T : Event>(
    key: String,
    private val event: Class<out T>
) : AttributeBase<CustomItem, Boolean>(key, true), Listener {
    override fun apply(obj: CustomItem) {
        Bukkit.getPluginManager().registerEvent(
            event,
            this,
            EventPriority.NORMAL,
            { _, event ->
                if (event.javaClass.isAssignableFrom(this.event)) onEvent(
                    obj,
                    event as T
                )
            },
            CustomItems.instance
        )
    }

    abstract fun onEvent(item: CustomItem, event: T)
}

object DropRestriction : Restriction<PlayerDropItemEvent>("cancel-drop", PlayerDropItemEvent::class.java) {
    override fun onEvent(item: CustomItem, event: PlayerDropItemEvent) {
        if (item.isCustomItem(event.itemDrop.itemStack))
            event.isCancelled = true
    }
}

object PlaceRestriction : Restriction<BlockPlaceEvent>("cancel-place", BlockPlaceEvent::class.java) {
    override fun onEvent(item: CustomItem, event: BlockPlaceEvent) {
        if (item.isCustomItem(event.itemInHand) && event.itemInHand.type.isBlock)
            event.isCancelled = true
    }
}

open class CraftRestriction(
    key: String,
) : Restriction<CraftItemEvent>(key, CraftItemEvent::class.java) {

    constructor() : this("cancel-craft")

    override fun onEvent(item: CustomItem, event: CraftItemEvent) {
        if (event.inventory.any(item::isCustomItem))
            event.isCancelled = true
    }

}

object VanillaCraftRestriction : CraftRestriction("cancel-craft-vanilla") {
    override fun onEvent(item: CustomItem, event: CraftItemEvent) {
        if (event.recipe is Keyed && (event.recipe as Keyed).key.namespace() == NamespacedKey.MINECRAFT)
            super.onEvent(item, event)
    }
}

class StorageRestriction(
    private val types: Set<InventoryType>
) : Restriction<InventoryClickEvent>("cancel-storage", InventoryClickEvent::class.java) {
    override fun onEvent(item: CustomItem, event: InventoryClickEvent) {
        if (item.isCustomItem(event.currentItem) && types.contains(event.view.topInventory.type))
            event.isCancelled = true
    }
}

class DamageRestriction(
    private val causes: Set<DamageCause>
) : Restriction<EntityDamageEvent>("cancel-damage", EntityDamageEvent::class.java) {
    override fun onEvent(item: CustomItem, event: EntityDamageEvent) {
        if (event.entityType == EntityType.DROPPED_ITEM && item.isCustomItem((event.entity as Item).itemStack) && causes.contains(event.cause))
            event.isCancelled = true
    }
}