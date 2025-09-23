package org.bigcraft.customitems.activator.condition

import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import org.bigcraft.customitems.activator.test
import org.bigcraft.customitems.config.getIntComparator
import org.bigcraft.customitems.config.getEnumSet
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

val ACTIVATOR_CONDITION_MAP = AttributeMap(
    mapOf(
        "chance" to AttributeFactory { key, config -> ChanceCondition(key, config.getDouble(key)) },
        "is-sneaking" to AttributeFactory { key, config -> SneakingCondition(key, config.getBoolean(key)) },
        "is-sprinting" to AttributeFactory { key, config -> SprintingCondition(key, config.getBoolean(key)) },
        "is-flying" to AttributeFactory { key, config -> FlyingCondition(key, config.getBoolean(key)) },
        "is-blocking" to AttributeFactory { key, config -> BlockingCondition(key, config.getBoolean(key)) },
        "is-gliding" to AttributeFactory { key, config -> GlidingCondition(key, config.getBoolean(key)) },
        "is-swimming" to AttributeFactory { key, config -> SwimmingCondition(key, config.getBoolean(key)) },
        "is-on-fire" to AttributeFactory { key, config -> OnFireCondition(key, config.getBoolean(key)) },
        "is-in-air" to AttributeFactory { key, config -> InAirCondition(key, config.getBoolean(key)) },
        "is-light-level" to AttributeFactory { key, config -> LightLevelCondition(key, config.getIntComparator(key)) },
        "has-level" to AttributeFactory { key, config -> LevelCondition(key, config.getIntComparator(key)) },
        "has-exp" to AttributeFactory { key, config -> ExpCondition(key, config.getIntComparator(key)) },
        "has-food-level" to AttributeFactory { key, config -> FoodLevelCondition(key, config.getIntComparator(key)) },
        "has-health" to AttributeFactory { key, config -> HealthCondition(key, config.getIntComparator(key)) },
        "has-permissions" to AttributeFactory { key, config -> HasPermissionsCondition(key, config.getStringList(key).toSet()) },
        "not-has-permissions" to AttributeFactory { key, config -> NotHasPermissionsCondition(key, config.getStringList(key).toSet()) },
        "has-target-block" to AttributeFactory { key, config -> TargetBlockCondition(key, config.getEnumSet<Material>(key)) },
        "not-has-target-block" to AttributeFactory { key, config -> NotTargetBlockCondition(key, config.getEnumSet<Material>(key)) },
        "is-on-block" to AttributeFactory { key, config -> OnBlockCondition(key, config.getEnumSet<Material>(key)) },
        "is-not-on-block" to AttributeFactory { key, config -> NotOnBlockCondition(key, config.getEnumSet<Material>(key)) },
        "is-in-biome" to AttributeFactory { key, config -> InBiomeCondition(key, config.getEnumSet<Biome>(key)) },
        "is-not-in-biome" to AttributeFactory { key, config -> NotInBiomeCondition(key, config.getEnumSet<Biome>(key)) },
        "is-in-world" to AttributeFactory { key, config -> InWorldCondition(key, config.getStringList(key).mapNotNull { Bukkit.getWorld(it) }.toSet()) },
        "is-not-in-world" to AttributeFactory { key, config -> NotInWorldCondition(key, config.getStringList(key).mapNotNull { Bukkit.getWorld(it) }.toSet()) },
        "is-weather" to AttributeFactory { key, config -> WeatherCondition(key, config.getEnumSet<Weather>(key)) },
        "is-day-time" to AttributeFactory { key, config -> DayTimeCondition(key, config.getIntComparator(key)) },
        "is-item-durability" to AttributeFactory { key, config -> DurabilityCondition(key, config.getIntComparator(key)) },
        "is-item-cooldown" to AttributeFactory { key, config -> CooldownCondition(key, config.getIntComparator(key)) },
        "is-item-equipment" to AttributeFactory { key, config -> ItemEquipmentCondition(key, config.getEnumSet<EquipmentSlot>(key)) },
        "is-damager-type" to AttributeFactory { key, config -> DamagerTypeCondition(key, config.getEnumSet<EntityType>(key)) },
        "is-entity-type" to AttributeFactory { key, config -> EntityTypeCondition(key, config.getEnumSet<EntityType>(key)) },
        "has-damager-meta" to AttributeFactory { key, config -> DamagerMetaCondition(key, config.getStringList(key).toSet()) },
        "has-entity-meta" to AttributeFactory { key, config -> EntityMetaCondition(key, config.getStringList(key).toSet()) },
        "is-damage-cause" to AttributeFactory { key, config -> DamageCauseCondition(key, config.getEnumSet<EntityDamageEvent.DamageCause>(key)) },
        "is-potion-action" to AttributeFactory { key, config -> PotionActionCondition(key, config.getEnumSet<EntityPotionEffectEvent.Action>(key)) },
        "is-potion-type" to AttributeFactory { key, config -> PotionTypeCondition(key, config.getStringList(key)
                .mapNotNull { runCatching { PotionEffectType.getByName(it) }.getOrNull() }
                .toSet()) },
    )
)

abstract class ActivatorCondition<V>(key: String, value: V) : AttributeBase<V>(key, value) {
    abstract fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean
}

class ChanceCondition(key: String, chance: Double) : ActivatorCondition<Double>(key, chance) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.test()
}