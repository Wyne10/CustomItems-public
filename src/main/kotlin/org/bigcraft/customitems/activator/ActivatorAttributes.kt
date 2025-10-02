package org.bigcraft.customitems.activator

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import me.wyne.wutils.common.Ticks
import me.wyne.wutils.config.configurables.attribute.Attribute
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.item.attribute.PotionEffectsAttribute
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import me.wyne.wutils.i18n.kotlin.placeholderString
import me.wyne.wutils.i18n.kotlin.replace
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.attribute.JackOLanternAttribute
import org.bigcraft.customitems.attribute.PlayerExplosionAttribute
import org.bigcraft.customitems.attribute.RepairAttribute
import org.bigcraft.customitems.block.CustomBlockManager
import org.bigcraft.customitems.config.DoubleOperation
import org.bigcraft.customitems.config.IntOperation
import org.bigcraft.customitems.config.getDoubleOperation
import org.bigcraft.customitems.config.getEnumSet
import org.bigcraft.customitems.config.getIntOperation
import org.bigcraft.customitems.config.getMillis
import org.bigcraft.customitems.config.getPotionTypeEnumSet
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.block.BlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.roundToInt
import kotlin.random.Random

val ACTIVATOR_ATTRIBUTE_MAP = AttributeMap(
    mapOf(
        "condition-cancel-event" to AttributeFactory { key, config -> CancelEventAttribute(key, config.getBoolean(key)) },
        "cooldown-cancel-event" to AttributeFactory { key, config -> CancelEventAttribute(key, config.getBoolean(key)) },
        "cancel-event" to AttributeFactory { key, config -> CancelEventAttribute(key, config.getBoolean(key)) },
        "commands" to AttributeFactory { key, config -> CommandsAttribute(key, config.getStringList(key)) },
        "steal-level" to AttributeFactory { key, config -> LevelStealAttribute(key, config.getDouble(key)) },
        "entity-potion-effects" to EntityPotionEffectsAttribute.Factory,
        "player-potion-effects" to PlayerPotionEffectsAttribute.Factory,
        "entity-remove-effects" to AttributeFactory { key, config -> EntityRemoveEffectsAttribute(key, config.getPotionTypeEnumSet(key)) },
        "player-remove-effects" to AttributeFactory { key, config -> PlayerRemoveEffectsAttribute(key, config.getPotionTypeEnumSet(key)) },
        "consume-projectile" to AttributeFactory { key, config -> ConsumeProjectileAttribute(key, config.getBoolean(key)) },
        "set-projectile-meta" to AttributeFactory { key, config -> ProjectileMetaAttribute(key, config.getString(key)!!) },
        "set-damager-health" to AttributeFactory { key, config -> DamagerHealthAttribute(key, config.getDoubleOperation(key)) },
        "lock-drop" to AttributeFactory { key, config -> DropLockAttribute(key, config.getMillis(key)) },
        "player-explosion" to PlayerExplosionAttribute.Factory,
        "set-item-amount" to AttributeFactory { key, config -> ItemAmountAttribute(key, config.getIntOperation(key)) },
        "jack-o-lantern" to JackOLanternAttribute.Factory,
        "repair" to AttributeFactory { key, config -> RepairAttribute(key, config.getEnumSet<EquipmentSlot>(key)) },
        "force-field" to org.bigcraft.customitems.attribute.ForceFieldAttribute.Factory,
        "action-bar" to AttributeFactory { key, config -> ActionBarAttribute(key, config.getString(key)!!) },
        "set-player-exp" to AttributeFactory { key, config -> PlayerExpAttribute(key, config.getIntOperation(key)) },
        "set-custom-block" to AttributeFactory { key, config -> SetCustomBlockAttribute(key, config.getString(key)!!) }
    )
)

abstract class ActivatorAttribute<V>(key: String, value: V) : AttributeBase<V>(key, value) {
    open fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) = true
    abstract fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack)
}

class CancelEventAttribute(
    key: String,
    value: Boolean
) : ActivatorAttribute<Boolean>(key, value) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is Cancellable

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as Cancellable
        event.isCancelled = value
    }
}

class CommandsAttribute(
    key: String,
    commands: List<String>
) : ActivatorAttribute<List<String>>(key, commands) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        value.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), player.placeholderString(it).get()) }
    }
}

class LevelStealAttribute(
    key: String,
    steal: Double
) : ActivatorAttribute<Double>(key, steal) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is PlayerDeathEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as PlayerDeathEvent
        val killedPlayer = event.entity
        val toSteal = killedPlayer.level * value
        player.level += toSteal.toInt()
    }
}

class EntityPotionEffectsAttribute(
    key: String,
    effects: Set<PotionEffect>
) : ActivatorAttribute<Set<PotionEffect>>(key, effects) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is EntityEvent && event.entity is LivingEntity

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as EntityEvent
        val target = event.entity as LivingEntity
        value.forEach { potionEffect ->
            potionEffect.apply(target)
        }
    }

    object Factory : AttributeFactory {
        override fun create(key: String, config: ConfigurationSection): Attribute<*> {
            return EntityPotionEffectsAttribute(
                key,
                PotionEffectsAttribute(key, config).value.map { it.value }.toSet()
            )
        }
    }
}

class EntityRemoveEffectsAttribute(
    key: String,
    effects: Set<PotionEffectType>
) : ActivatorAttribute<Set<PotionEffectType>>(key, effects) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is EntityEvent && event.entity is LivingEntity

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as EntityEvent
        val target = event.entity as LivingEntity
        value.forEach { potionEffect ->
            target.removePotionEffect(potionEffect)
        }
    }
}

class PlayerPotionEffectsAttribute(
    key: String,
    effects: Set<PotionEffect>
) : ActivatorAttribute<Set<PotionEffect>>(key, effects) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        Bukkit.getScheduler().runTaskLater(CustomItems.instance, Runnable {
            value.forEach { potionEffect ->
                potionEffect.apply(player)
            }
        }, 1L)
    }

    object Factory : AttributeFactory {
        override fun create(key: String, config: ConfigurationSection): Attribute<*> {
            return PlayerPotionEffectsAttribute(
                key,
                PotionEffectsAttribute(key, config).value.map { it.value }.toSet()
            )
        }
    }
}

class PlayerRemoveEffectsAttribute(
    key: String,
    effects: Set<PotionEffectType>
) : ActivatorAttribute<Set<PotionEffectType>>(key, effects) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        value.forEach { potionEffect ->
            player.removePotionEffect(potionEffect)
        }
    }
}

class ConsumeProjectileAttribute(
    key: String,
    consume: Boolean
) : ActivatorAttribute<Boolean>(key, consume) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is PlayerLaunchProjectileEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as PlayerLaunchProjectileEvent
        event.setShouldConsume(value)
    }
}

class ProjectileMetaAttribute(
    key: String,
    meta: String
) : ActivatorAttribute<String>(key, meta) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is PlayerLaunchProjectileEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as PlayerLaunchProjectileEvent
        event.projectile.setMetadata(value, FixedMetadataValue(CustomItems.instance, value))
    }
}

class DamagerHealthAttribute(
    key: String,
    operation: DoubleOperation
) : ActivatorAttribute<DoubleOperation>(key, operation) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is EntityDamageByEntityEvent && event.damager is LivingEntity

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as EntityDamageByEntityEvent
        val damager = event.damager as LivingEntity
        damager.health = value.evaluate(damager.health)
    }
}

class DropLockAttribute(
    key: String,
    durationMillis: Long
) : ActivatorAttribute<Long>(key, durationMillis) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is PlayerDeathEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as PlayerDeathEvent
        val killedPlayer = event.entity
        val drops = event.drops.toList()
        event.drops.clear()
        val items = drops
            .map { killedPlayer.world.dropItem(killedPlayer.location, it) }
        items.forEach { it.owner = player.uniqueId }
        Bukkit.getScheduler().runTaskLater(CustomItems.instance, Runnable {
            items.forEach { it.owner = null }
        }, Ticks.ofMillis(value))
    }
}

class ItemAmountAttribute(
    key: String,
    operation: IntOperation
) : ActivatorAttribute<IntOperation>(key, operation) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        item.amount = value.evaluate(item.amount)
    }
}

class ActionBarAttribute(
    key: String,
    message: String
) : ActivatorAttribute<String>(key, message) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        player.placeholderComponent(value, "key" replace customItem.key).sendActionBar(player)
    }
}

class PlayerExpAttribute(
    key: String,
    operation: IntOperation
) : ActivatorAttribute<IntOperation>(key, operation) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val currentExp = player.currentExp
        player.exp = 0f
        player.level = 0
        player.giveExp(value.evaluate(currentExp))
    }
}

class SetCustomBlockAttribute(
    key: String,
    blockKey: String
) : ActivatorAttribute<String>(key, blockKey) {

    private val customBlock by lazy {
        CustomItemManager.instance.getItem(value)!!
    }

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        customBlock.isBlock && (event is BlockEvent || event is PlayerInteractEvent)

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        if (event is BlockEvent) {
            val block = event.block
            if (block.type != customBlock.material)
                block.type = customBlock.material
            CustomBlockManager.instance.placeBlock(block, customBlock)
        } else if (event is PlayerInteractEvent) {
            val block = event.clickedBlock ?: return
            if (block.type != customBlock.material)
                block.type = customBlock.material
            CustomBlockManager.instance.placeBlock(block, customBlock)
        }
    }
}

fun Double.test(): Boolean =
    Random.nextDouble() < this

/**
 * One can determine how much experience has been collected to reach a level using the equations:
 *
 * ```
 * Total experience =
 * level^2 + 6 × level (at levels 0–16)
 * 2.5 × level^2 – 40.5 × level + 360 (at levels 17–31)
 * 4.5 × level^2 – 162.5 × level + 2220 (at levels 32+)
 * ```
 */
val Player.levelExp: Int
    get() =
        when {
            level <= 16 -> (level * level) + 6 * level
            level <= 31 -> 2.5 * (level * level) - 40.5 * level + 360
            else -> 4.5 * (level * level) - 162.5 * level + 2220
        }.toInt()

/**
 * The formulas for figuring out how many experience orbs needed to get to the next level are as follows:
 *
 * ```
 * Experience required =
 * 2 × current_level + 7 (for levels 0–15)
 * 5 × current_level – 38 (for levels 16–30)
 * 9 × current_level – 158 (for levels 31+)
 * ```
 */
val Player.expToLevelUp: Int
    get() =
        when {
            level <= 15 -> 2 * level + 7
            level <= 30 -> 5 * level - 38
            else -> 9 * level - 158
        }

val Player.currentExp: Int
    get() {
        var currentExp = 0
        currentExp += levelExp
        currentExp += (expToLevelUp * exp).roundToInt()
        return currentExp
    }