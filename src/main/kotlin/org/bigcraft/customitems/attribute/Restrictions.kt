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

val Restrictions = AttributeMap(
    mutableMapOf(
        "cancel-drop" to AttributeFactory { _, _ -> DropRestriction },
        "cancel-place" to AttributeFactory { _, _ -> PlaceRestriction },
        "cancel-craft" to AttributeFactory { _, _ -> CraftRestriction() },
        "cancel-craft-vanilla" to AttributeFactory { _, _ -> VanillaCraftRestriction },
        "cancel-storage" to AttributeFactory { key, config -> StorageRestriction(config.getEnumSet<InventoryType>(key)) },
        "cancel-damage" to AttributeFactory { key, config -> DamageRestriction(config.getEnumSet<DamageCause>(key)) },
        "cancel-despawn" to AttributeFactory { _, _ -> DespawnRestriction },
        "cancel-enchant" to AttributeFactory { _, _ -> EnchantRestriction },
        "cancel-enchant-prepare" to AttributeFactory { _, _ -> EnchantPrepareRestriction },
        "cancel-consumption" to AttributeFactory { _, _ -> ConsumptionRestriction },
        "cancel-rename" to AttributeFactory { _, _ -> RenameRestriction }
    )
)

abstract class Restriction<T : Event>(
    key: String,
    val event: Class<out T>
) : AttributeBase<CustomItem, Boolean>(key, true) {

    protected lateinit var item: CustomItem
        private set

    final override fun apply(obj: CustomItem) {
        item = obj
        RestrictionListener.register(this)
    }

    abstract fun onEvent(event: T)
}

object RestrictionListener : Listener, AutoCloseable {

    private val registry: MutableMap<Class<out Event>, MutableSet<Restriction<*>>> = HashMap()

    fun <T : Event> register(restriction: Restriction<T>) {
        if (!registry.containsKey(restriction.event)) {
            registry[restriction.event] = HashSet()
            registerEvent(restriction.event)
        }
        registry[restriction.event]!!.add(restriction)
    }

    override fun close() {
        registry.values.forEach { it.clear() }
    }

    private fun <T : Event> registerEvent(event: Class<out T>) {
        Bukkit.getPluginManager().registerEvent(
            event,
            this,
            EventPriority.NORMAL,
            { _, calledEvent ->
                if (calledEvent.javaClass.isAssignableFrom(event))
                    registry[event]!!.forEach { (it as Restriction<T>).onEvent(calledEvent as T) }
            },
            CustomItems.instance,
            true
        )
    }

}

object DropRestriction : Restriction<PlayerDropItemEvent>("cancel-drop", PlayerDropItemEvent::class.java) {
    override fun onEvent(event: PlayerDropItemEvent) {
        if (item isCustomItem event.itemDrop.itemStack)
            event.isCancelled = true
    }
}

object PlaceRestriction : Restriction<BlockPlaceEvent>("cancel-place", BlockPlaceEvent::class.java) {
    override fun onEvent(event: BlockPlaceEvent) {
        if (item isCustomItem event.itemInHand && event.itemInHand.type.isBlock)
            event.isCancelled = true
    }
}

open class CraftRestriction(
    key: String,
) : Restriction<CraftItemEvent>(key, CraftItemEvent::class.java) {

    constructor() : this("cancel-craft")

    override fun onEvent(event: CraftItemEvent) {
        if (event.inventory.any(item::isCustomItem))
            event.isCancelled = true
    }

}

object VanillaCraftRestriction : CraftRestriction("cancel-craft-vanilla") {
    override fun onEvent(event: CraftItemEvent) {
        if (event.recipe is Keyed && (event.recipe as Keyed).key.namespace() == NamespacedKey.MINECRAFT)
            super.onEvent(event)
    }
}

class StorageRestriction(
    private val types: Set<InventoryType>
) : Restriction<InventoryClickEvent>("cancel-storage", InventoryClickEvent::class.java) {
    override fun onEvent(event: InventoryClickEvent) {
        if (item isCustomItem event.currentItem && types.contains(event.view.topInventory.type))
            event.isCancelled = true
    }
}

class DamageRestriction(
    private val causes: Set<DamageCause>
) : Restriction<EntityDamageEvent>("cancel-damage", EntityDamageEvent::class.java) {
    override fun onEvent(event: EntityDamageEvent) {
        if (event.entityType == EntityType.DROPPED_ITEM && item isCustomItem (event.entity as Item).itemStack && causes.contains(event.cause))
            event.isCancelled = true
    }
}

object DespawnRestriction : Restriction<ItemDespawnEvent>("cancel-despawn", ItemDespawnEvent::class.java) {
    override fun onEvent(event: ItemDespawnEvent) {
        if (item isCustomItem event.entity.itemStack)
            event.isCancelled = true
    }
}

object EnchantRestriction : Restriction<EnchantItemEvent>("cancel-enchant", EnchantItemEvent::class.java) {
    override fun onEvent(event: EnchantItemEvent) {
        if (item isCustomItem event.item)
            event.isCancelled = true
    }
}

object EnchantPrepareRestriction : Restriction<PrepareItemEnchantEvent>("cancel-enchant-prepare", PrepareItemEnchantEvent::class.java) {
    override fun onEvent(event: PrepareItemEnchantEvent) {
        if (item isCustomItem event.item)
            event.isCancelled = true
    }
}

object ConsumptionRestriction : Restriction<PlayerItemConsumeEvent>("cancel-consumption", PlayerItemConsumeEvent::class.java) {
    override fun onEvent(event: PlayerItemConsumeEvent) {
        if (item isCustomItem event.item)
            event.isCancelled = true
    }
}

object RenameRestriction : Restriction<InventoryClickEvent>("cancel-rename", InventoryClickEvent::class.java) {
    override fun onEvent(event: InventoryClickEvent) {
        if (event.clickedInventory !is AnvilInventory) return
        val inventory = event.clickedInventory as AnvilInventory
        if (inventory.renameText != null || inventory.renameText?.isNotEmpty() == true)
            event.isCancelled = true
    }
}
