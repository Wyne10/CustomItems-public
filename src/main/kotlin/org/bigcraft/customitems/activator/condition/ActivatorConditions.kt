package org.bigcraft.customitems.activator.condition

import me.wyne.wutils.common.kotlin.config.getDoubleComparator
import me.wyne.wutils.common.kotlin.config.getEnumSet
import me.wyne.wutils.common.kotlin.config.getIntComparator
import me.wyne.wutils.common.kotlin.config.getPotionTypeEnumSet
import me.wyne.wutils.common.kotlin.doubles.test
import org.bigcraft.customitems.api.CustomItemApi
import org.bigcraft.customitems.api.spi.ActivatorCondition as ActivatorConditionApi
import org.bigcraft.customitems.api.spi.ConfigFactory
import org.bigcraft.customitems.api.spi.FactoryRegistry
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.worldguard.RegionPatterns
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

internal fun registerActivatorConditions(registry: FactoryRegistry<ActivatorConditionApi>) {
    registry.register("chance") { key, config -> ChanceCondition(key, config.getDouble(key)) }
    registry.register("is-sneaking") { key, config -> SneakingCondition(key, config.getBoolean(key)) }
    registry.register("is-sprinting") { key, config -> SprintingCondition(key, config.getBoolean(key)) }
    registry.register("is-flying") { key, config -> FlyingCondition(key, config.getBoolean(key)) }
    registry.register("is-blocking") { key, config -> BlockingCondition(key, config.getBoolean(key)) }
    registry.register("is-gliding") { key, config -> GlidingCondition(key, config.getBoolean(key)) }
    registry.register("is-swimming") { key, config -> SwimmingCondition(key, config.getBoolean(key)) }
    registry.register("is-on-fire") { key, config -> OnFireCondition(key, config.getBoolean(key)) }
    registry.register("is-in-air") { key, config -> InAirCondition(key, config.getBoolean(key)) }
    registry.register("is-light-level") { key, config -> LightLevelCondition(key, config.getIntComparator(key)) }
    registry.register("is-level") { key, config -> LevelCondition(key, config.getIntComparator(key)) }
    registry.register("is-exp") { key, config -> ExpCondition(key, config.getIntComparator(key)) }
    registry.register("is-food-level") { key, config -> FoodLevelCondition(key, config.getIntComparator(key)) }
    registry.register("is-health") { key, config -> HealthCondition(key, config.getDoubleComparator(key)) }
    registry.register("has-permissions") { key, config -> HasPermissionsCondition(key, config.getStringList(key).toSet()) }
    registry.register("not-has-permissions") { key, config -> NotHasPermissionsCondition(key, config.getStringList(key).toSet()) }
    registry.registerInvertible("is-target-block") { key, config -> TargetBlockCondition(key, config.getEnumSet<Material>(key)) }
    registry.registerInvertible("is-on-block") { key, config -> OnBlockCondition(key, config.getEnumSet<Material>(key)) }
    registry.registerInvertible("is-in-biome") { key, config -> InBiomeCondition(key, config.getEnumSet<Biome>(key)) }
    registry.registerInvertible("is-in-world") { key, config -> InWorldCondition(key, config.getStringList(key).mapNotNull { Bukkit.getWorld(it) }.toSet()) }
    registry.registerInvertible("is-within-aoe") { key, config -> WithinAoeCondition(key, config.getStringList(key).toSet()) }
    registry.registerInvertible("block-is-within-aoe", notKey = "block-not-is-within-aoe") { key, config -> BlockWithinAoeCondition(key, config.getStringList(key).toSet()) }
    registry.registerInvertible("is-aoe-affected") { key, config -> AoeAffectedCondition(key, config.getStringList(key).toSet()) }
    registry.registerInvertible("is-within-region") { key, config -> WithinRegionCondition(key, RegionPatterns.of(config.getStringList(key))) }
    registry.registerInvertible("is-within-top-region") { key, config -> WithinTopRegionCondition(key, RegionPatterns.of(config.getStringList(key))) }
    registry.register("is-pvp") { key, config -> PvpCondition(key, config.getBoolean(key)) }
    registry.register("is-aoe-finished") { key, config -> AoeFinishConditions(key, config.getStringList(key).toSet()) }
    registry.register("is-weather") { key, config -> WeatherCondition(key, config.getEnumSet<Weather>(key)) }
    registry.register("is-day-time") { key, config -> DayTimeCondition(key, config.getIntComparator(key)) }
    registry.register("is-item-durability") { key, config -> ItemDurabilityCondition(key, config.getIntComparator(key)) }
    registry.register("is-item-cooldown") { key, config -> ItemCooldownCondition(key, config.getIntComparator(key)) }
    registry.register("is-custom-item-cooldowned") { key, config -> CustomItemCooldownCondition(key, config.getBoolean(key)) }
    registry.register("is-item-equipment") { key, config -> ItemEquipmentCondition(key, config.getEnumSet<EquipmentSlot>(key)) }
    registry.register("is-item-composed") { key, config -> ItemComposedCondition(key, config.getBoolean(key)) }
    registry.register("is-item-type") { key, config -> ItemTypeCondition(key, config.getEnumSet<Material>(key)) }
    registry.register("is-block-type") { key, config -> BlockTypeCondition(key, config.getEnumSet<Material>(key)) }
    registry.registerInvertible("has-block-meta") { key, config -> BlockMetaCondition(key, config.getStringList(key).toSet()) }
    registry.register("is-consumed-item-type") { key, config -> ConsumedItemTypeCondition(key, config.getEnumSet<Material>(key)) }
    registry.register("is-consumed-item-custom") { key, config -> ConsumedItemCustomCondition(key, config.getBoolean(key)) }
    registry.register("is-dropped-item-custom") { key, config -> DroppedItemCustomCondition(key, config.getBoolean(key)) }
    registry.register("is-potion-type") { key, config -> PotionTypeCondition(key, config.getPotionTypeEnumSet(key)) }
    registry.register("is-damager-type") { key, config -> DamagerTypeCondition(key, config.getEnumSet<EntityType>(key)) }
    registry.register("is-entity-type") { key, config -> EntityTypeCondition(key, config.getEnumSet<EntityType>(key)) }
    registry.register("is-projectile-type") { key, config -> ProjectileTypeCondition(key, config.getEnumSet<EntityType>(key)) }
    registry.register("has-damager-meta") { key, config -> DamagerMetaCondition(key, config.getStringList(key).toSet()) }
    registry.register("has-entity-meta") { key, config -> EntityMetaCondition(key, config.getStringList(key).toSet()) }
    registry.register("is-damage-cause") { key, config -> DamageCauseCondition(key, config.getEnumSet<EntityDamageEvent.DamageCause>(key)) }
    registry.register("is-effect-action") { key, config -> EffectActionCondition(key, config.getEnumSet<EntityPotionEffectEvent.Action>(key)) }
    registry.register("is-effect-type") { key, config -> EffectTypeCondition(key, config.getPotionTypeEnumSet(key)) }
    registry.register("is-effect-cause") { key, config -> EffectCauseCondition(key, config.getEnumSet<EntityPotionEffectEvent.Cause>(key)) }
    registry.register("command-whitelist") { key, config -> CommandWhitelistCondition(key, config.getStringList(key).toSet()) }
}

/**
 * Registers a condition under [key] and its negation under [notKey], rather than writing the
 * opposite condition by hand.
 *
 * Inversion flips the verdict only: a condition that does not apply to an event still does not
 * apply, so a `not-` variant of an event-scoped condition keeps passing events it says nothing
 * about.
 */
private fun FactoryRegistry<ActivatorConditionApi>.registerInvertible(
    key: String,
    notKey: String = "not-$key",
    factory: ConfigFactory<out ActivatorConditionApi>,
) {
    register(key, factory)
    register(notKey) { resolvedKey, config -> factory.create(resolvedKey, config).inverted() }
}

abstract class ActivatorCondition<V : Any>(val key: String, val value: V) : ActivatorConditionApi {

    final override fun test(event: Event, player: Player, item: CustomItemApi, stack: ItemStack): Boolean =
        isApplicable(event, player, item as CustomItem, stack)

    abstract fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean
}

class ChanceCondition(key: String, chance: Double) : ActivatorCondition<Double>(key, chance) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.test()
}