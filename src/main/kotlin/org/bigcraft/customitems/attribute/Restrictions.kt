package org.bigcraft.customitems.attribute

import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.extension.getEnumSet
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemAttribute
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

val ITEM_RESTRICTION_MAP = AttributeMap(
    mutableMapOf(
        "cancel-drop" to AttributeFactory { key, _ -> DropRestriction(key) },
        "cancel-place" to AttributeFactory { key, _ -> PlaceRestriction(key) },
        "cancel-craft" to AttributeFactory { key, _ -> CraftRestriction(key) },
        "cancel-craft-vanilla" to AttributeFactory { key, _ -> VanillaCraftRestriction(key) },
        "cancel-storage" to AttributeFactory { key, config -> StorageRestriction(key, config.getEnumSet<InventoryType>(key)) },
        "cancel-damage" to AttributeFactory { key, config -> DamageRestriction(key, config.getEnumSet<DamageCause>(key)) },
        "cancel-despawn" to AttributeFactory { key, _ -> DespawnRestriction(key) },
        "cancel-enchant" to AttributeFactory { key, _ -> EnchantRestriction(key) },
        "cancel-enchant-prepare" to AttributeFactory { key, _ -> EnchantPrepareRestriction(key) },
        "cancel-consumption" to AttributeFactory { key, _ -> ConsumptionRestriction(key) },
        "cancel-rename" to AttributeFactory { key, _ -> RenameRestriction(key) },
        "cancel-anvil" to AttributeFactory { key, _ -> AnvilRestriction(key) }
    )
)

abstract class Restriction<T : Event>(
    key: String,
    val event: Class<out T>
) : AttributeBase<Boolean>(key, true), CustomItemAttribute {

    protected lateinit var item: CustomItem
        private set

    final override fun apply(item: CustomItem) {
        this.item = item
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

    @Suppress("UNCHECKED_CAST")
    private fun <T : Event> registerEvent(event: Class<out T>) {
        Bukkit.getPluginManager().registerEvent(
            event,
            this,
            EventPriority.NORMAL,
            { _, calledEvent ->
                if (calledEvent.javaClass == event)
                    registry[event]!!.forEach { (it as Restriction<T>).onEvent(calledEvent as T) }
            },
            CustomItems.instance,
            true
        )
    }

}

class DropRestriction(key: String) : Restriction<PlayerDropItemEvent>(key, PlayerDropItemEvent::class.java) {
    override fun onEvent(event: PlayerDropItemEvent) {
        if (item isCustomItem event.itemDrop.itemStack)
            event.isCancelled = true
    }
}

class PlaceRestriction(key: String) : Restriction<BlockPlaceEvent>(key, BlockPlaceEvent::class.java) {
    override fun onEvent(event: BlockPlaceEvent) {
        if (item isCustomItem event.itemInHand && event.itemInHand.type.isBlock)
            event.isCancelled = true
    }
}

open class CraftRestriction(key: String) : Restriction<CraftItemEvent>(key, CraftItemEvent::class.java) {
    override fun onEvent(event: CraftItemEvent) {
        if (event.inventory.any(item::isCustomItem))
            event.isCancelled = true
    }
}

class VanillaCraftRestriction(key: String) : CraftRestriction(key) {
    override fun onEvent(event: CraftItemEvent) {
        if (event.recipe is Keyed && (event.recipe as Keyed).key.namespace() == NamespacedKey.MINECRAFT)
            super.onEvent(event)
    }
}

class StorageRestriction(
    key: String,
    private val types: Set<InventoryType>
) : Restriction<InventoryClickEvent>(key, InventoryClickEvent::class.java) {
    override fun onEvent(event: InventoryClickEvent) {
        if (item isCustomItem event.currentItem && types.contains(event.view.topInventory.type))
            event.isCancelled = true
    }
}

class DamageRestriction(
    key: String,
    private val causes: Set<DamageCause>
) : Restriction<EntityDamageEvent>(key, EntityDamageEvent::class.java) {
    override fun onEvent(event: EntityDamageEvent) {
        if (event.entityType == EntityType.DROPPED_ITEM && item isCustomItem (event.entity as Item).itemStack && causes.contains(event.cause))
            event.isCancelled = true
    }
}

class DespawnRestriction(key: String) : Restriction<ItemDespawnEvent>(key, ItemDespawnEvent::class.java) {
    override fun onEvent(event: ItemDespawnEvent) {
        if (item isCustomItem event.entity.itemStack)
            event.isCancelled = true
    }
}

class EnchantRestriction(key: String) : Restriction<EnchantItemEvent>(key, EnchantItemEvent::class.java) {
    override fun onEvent(event: EnchantItemEvent) {
        if (item isCustomItem event.item)
            event.isCancelled = true
    }
}

class EnchantPrepareRestriction(key: String) : Restriction<PrepareItemEnchantEvent>(key, PrepareItemEnchantEvent::class.java) {
    override fun onEvent(event: PrepareItemEnchantEvent) {
        if (item isCustomItem event.item)
            event.isCancelled = true
    }
}

class ConsumptionRestriction(key: String) : Restriction<PlayerItemConsumeEvent>(key, PlayerItemConsumeEvent::class.java) {
    override fun onEvent(event: PlayerItemConsumeEvent) {
        if (item isCustomItem event.item)
            event.isCancelled = true
    }
}

class RenameRestriction(key: String) : Restriction<InventoryClickEvent>(key, InventoryClickEvent::class.java) {
    override fun onEvent(event: InventoryClickEvent) {
        if (event.clickedInventory !is AnvilInventory) return
        val inventory = event.clickedInventory as AnvilInventory
        if (event.slot == 2 && (inventory.renameText != null || inventory.renameText?.isNotEmpty() == true))
            event.isCancelled = true
    }
}

class AnvilRestriction(key: String) : Restriction<InventoryClickEvent>(key, InventoryClickEvent::class.java) {
    override fun onEvent(event: InventoryClickEvent) {
        if (event.clickedInventory !is AnvilInventory) return
        val inventory = event.clickedInventory as AnvilInventory
        if (event.slot == 2 && (item isCustomItem inventory.firstItem || item isCustomItem inventory.secondItem))
            event.isCancelled = true
    }
}