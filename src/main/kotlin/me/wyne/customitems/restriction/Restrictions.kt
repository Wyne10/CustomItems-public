package me.wyne.customitems.restriction

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import me.wyne.wutils.common.kotlin.config.getEnumSet
import me.wyne.wutils.common.kotlin.inventory.getAffectedItems
import me.wyne.wutils.i18n.kotlin.legacy
import me.wyne.wutils.i18n.kotlin.plainText
import me.wyne.customitems.api.spi.InstanceRegistry
import me.wyne.customitems.api.spi.RestrictionType
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
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
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.AnvilInventory

internal fun registerRestrictionTypes(registry: InstanceRegistry<RestrictionType<*>>) {
    registry.register(DropRestriction)
    registry.register(PlaceRestriction)
    registry.register(CraftRestriction)
    registry.register(VanillaCraftRestriction)
    registry.register(SmeltingRestriction)
    registry.register(StorageRestriction)
    registry.register(AllowStorageRestriction)
    registry.register(StorageTitleRestriction)
    registry.register(DamageRestriction)
    registry.register(DespawnRestriction)
    registry.register(EnchantRestriction)
    registry.register(EnchantPrepareRestriction)
    registry.register(ConsumptionRestriction)
    registry.register(RenameRestriction)
    registry.register(AnvilRestriction)
    registry.register(ProjectileRestriction)
    registry.register(ActionRestriction)
    registry.register(EntityActionRestriction)
}

/** A restriction configured as a plain `key: true`. */
abstract class FlagRestriction(key: String) : RestrictionType<Boolean>(key) {
    override fun parse(key: String, config: ConfigurationSection): Boolean = config.getBoolean(key)
}

object DropRestriction : FlagRestriction("cancel-drop") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onDrop(event: PlayerDropItemEvent) = cancelIfBound(event, event.itemDrop.itemStack)
}

object PlaceRestriction : FlagRestriction("cancel-place") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onPlace(event: BlockPlaceEvent) {
        if (!event.itemInHand.type.isBlock) return
        cancelIfBound(event, event.itemInHand)
    }
}

object CraftRestriction : FlagRestriction("cancel-craft") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onCraft(event: CraftItemEvent) = forEachBinding { item, _ ->
        if (event.inventory.any(item::isCustomItem)) event.isCancelled = true
    }
}

object VanillaCraftRestriction : FlagRestriction("cancel-craft-vanilla") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onCraft(event: CraftItemEvent) {
        val recipe = event.recipe as? Keyed ?: return
        if (recipe.key.namespace() != NamespacedKey.MINECRAFT) return
        forEachBinding { item, _ ->
            if (event.inventory.any(item::isCustomItem)) event.isCancelled = true
        }
    }
}

object SmeltingRestriction : FlagRestriction("cancel-smelt") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onSmelt(event: FurnaceSmeltEvent) = cancelIfBound(event, event.source)
}

/** A restriction scoped to a set of inventory types. */
abstract class InventoryTypeRestriction(key: String) : RestrictionType<Set<InventoryType>>(key) {
    override fun parse(key: String, config: ConfigurationSection): Set<InventoryType> =
        config.getEnumSet<InventoryType>(key)

    protected abstract fun inScope(types: Set<InventoryType>, type: InventoryType): Boolean

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onClick(event: InventoryClickEvent) = forEachBinding { item, types ->
        if (!inScope(types, event.view.topInventory.type)) return@forEachBinding
        event.getAffectedItems().forEach { cancel(event, item, it) }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onPickup(event: InventoryPickupItemEvent) = forEachBinding { item, types ->
        if (!inScope(types, event.inventory.type)) return@forEachBinding
        cancel(event, item, event.item.itemStack)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onMove(event: InventoryMoveItemEvent) = forEachBinding { item, types ->
        if (!inScope(types, event.destination.type)) return@forEachBinding
        cancel(event, item, event.item)
    }
}

object StorageRestriction : InventoryTypeRestriction("cancel-storage") {
    override fun inScope(types: Set<InventoryType>, type: InventoryType) = type in types
}

object AllowStorageRestriction : InventoryTypeRestriction("allow-storage") {
    override fun inScope(types: Set<InventoryType>, type: InventoryType) = type !in types
}

object StorageTitleRestriction : RestrictionType<Regex>("cancel-storage-title") {
    override fun parse(key: String, config: ConfigurationSection): Regex =
        Regex(config.getString(key).orEmpty())

    override fun isActive(value: Regex) = value.pattern.isNotBlank()

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onClick(event: InventoryClickEvent) {
        val title = event.view.title.legacy.plainText
        forEachBinding { item, regex ->
            if (!regex.matches(title)) return@forEachBinding
            event.getAffectedItems().forEach { cancel(event, item, it) }
        }
    }
}

object DamageRestriction : RestrictionType<Set<DamageCause>>("cancel-damage") {
    override fun parse(key: String, config: ConfigurationSection): Set<DamageCause> =
        config.getEnumSet<DamageCause>(key)

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onDamage(event: EntityDamageEvent) {
        if (event.entityType != EntityType.DROPPED_ITEM) return
        val stack = (event.entity as Item).itemStack
        forEachBinding { item, causes ->
            if (event.cause !in causes) return@forEachBinding
            cancel(event, item, stack)
        }
    }
}

object DespawnRestriction : FlagRestriction("cancel-despawn") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onDespawn(event: ItemDespawnEvent) = cancelIfBound(event, event.entity.itemStack)
}

object EnchantRestriction : FlagRestriction("cancel-enchant") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onEnchant(event: EnchantItemEvent) = cancelIfBound(event, event.item)
}

object EnchantPrepareRestriction : FlagRestriction("cancel-enchant-prepare") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onPrepare(event: PrepareItemEnchantEvent) = cancelIfBound(event, event.item)
}

object ConsumptionRestriction : FlagRestriction("cancel-consume") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onConsume(event: PlayerItemConsumeEvent) = cancelIfBound(event, event.item)
}

object RenameRestriction : FlagRestriction("cancel-rename") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onClick(event: InventoryClickEvent) {
        val inventory = event.clickedInventory as? AnvilInventory ?: return
        if (event.slot != 2 || inventory.renameText.isNullOrEmpty()) return
        cancelIfBound(event, inventory.firstItem)
    }
}

object AnvilRestriction : FlagRestriction("cancel-anvil") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onClick(event: InventoryClickEvent) {
        val inventory = event.clickedInventory as? AnvilInventory ?: return
        if (event.slot != 2) return
        cancelIfBound(event, inventory.firstItem)
        cancelIfBound(event, inventory.secondItem)
    }
}

object ProjectileRestriction : FlagRestriction("cancel-projectile") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onLaunch(event: PlayerLaunchProjectileEvent) = cancelIfBound(event, event.itemStack)
}

object ActionRestriction : RestrictionType<Set<Action>>("cancel-action") {
    override fun parse(key: String, config: ConfigurationSection): Set<Action> =
        config.getEnumSet<Action>(key)

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onInteract(event: PlayerInteractEvent) = forEachBinding { item, actions ->
        if (event.action !in actions) return@forEachBinding
        cancel(event, item, event.item)
    }
}

object EntityActionRestriction : FlagRestriction("cancel-entity-action") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onInteractEntity(event: PlayerInteractEntityEvent) =
        cancelIfBound(event, event.player.inventory.getItem(event.hand))

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onInteractAtEntity(event: PlayerInteractAtEntityEvent) =
        cancelIfBound(event, event.player.inventory.getItem(event.hand))
}
