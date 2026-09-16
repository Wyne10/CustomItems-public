package org.bigcraft.customitems.activator.attribute

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import me.wyne.wutils.common.Args
import me.wyne.wutils.common.operation.DoubleOperation
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.activator.ActivatorAttribute
import org.bigcraft.customitems.api.spi.CompositeConfigFactory
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

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

class CauseEntityDamageAttribute(
    key: String,
    cause: EntityDamageEvent.DamageCause,
    damage: Double
) : CallEntityDamageAttribute(key, cause, damage) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as EntityEvent
        val event = EntityDamageByEntityEvent(player, event.entity, value.first, value.second)
        if (!event.callEvent()) return
        val damage = event.finalDamage
        val entity = event.entity as? LivingEntity ?: return
        entity.damage(damage)
    }

    object Factory : CompositeConfigFactory<CauseEntityDamageAttribute> {
        override fun fromSection(key: String, section: ConfigurationSection): CauseEntityDamageAttribute =
            CauseEntityDamageAttribute(
                key,
                runCatching { EntityDamageEvent.DamageCause.valueOf(section.getString("cause")!!) }.getOrDefault(
                    EntityDamageEvent.DamageCause.ENTITY_ATTACK
                ),
                section.getDouble("damage")
            )

        override fun fromString(key: String, string: String, config: ConfigurationSection): CauseEntityDamageAttribute {
            val args = Args(string)
            return CauseEntityDamageAttribute(
                key,
                runCatching { EntityDamageEvent.DamageCause.valueOf(args.get(0, "ENTITY_ATTACK")) }.getOrDefault(
                    EntityDamageEvent.DamageCause.ENTITY_ATTACK
                ),
                args.get(1, "0.0").toDouble()
            )
        }
    }
}
